/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.windmill.cache;

import android.os.SystemClock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by wally.yan on 2016/6/28.
 */
public class DiskBasedCache implements Cache {

    /**
     * Map of the Key, CacheHeader pairs
     */
    private final Map<String, CacheEntry> mEntries = new LinkedHashMap<String, CacheEntry>(16, .75f, true);

    /**
     * Total amount of space currently used by the cache in bytes.
     */
    private long mTotalSize = 0;

    /**
     * The root directory to use for the cache.
     */
    private final File mRootDirectory;

    /**
     * The maximum size of the cache in bytes.
     */
    private final int mMaxCacheSizeInBytes;

    /**
     * Default maximum disk usage in bytes.
     */
    private static final int DEFAULT_DISK_USAGE_BYTES = 5 * 1024 * 1024;

    /**
     * High water mark percentage for the cache
     */
    private static final float HYSTERESIS_FACTOR = 0.9f;


    /**
     * Constructs an instance of the DiskBasedCache at the specified directory.
     *
     * @param rootDirectory       The root directory of the cache.
     * @param maxCacheSizeInBytes The maximum size of the cache in bytes.
     */
    public DiskBasedCache(File rootDirectory, int maxCacheSizeInBytes) {
        mRootDirectory = rootDirectory;
        mMaxCacheSizeInBytes = maxCacheSizeInBytes;
    }

    /**
     * Constructs an instance of the DiskBasedCache at the specified directory using
     * the default maximum cache size of 5MB.
     *
     * @param rootDirectory The root directory of the cache.
     */
    public DiskBasedCache(File rootDirectory) {
        this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
    }

    /**
     * Clears the cache. Deletes all cached files from disk.
     */
    @Override
    public synchronized void clear() {
        File[] files = mRootDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        mEntries.clear();
        mTotalSize = 0;
    }

    /**
     * Returns the cache entry with the specified key if it exists, null otherwise.
     */
    @Override
    public synchronized CacheEntry get(String key) {
        CacheEntry cacheEntry = mEntries.get(key);
        // if the cacheEntry does not exist, return.
        if (cacheEntry == null) {
            return null;
        }

        File file = getFileForKey(key);
        CountingInputStream cis = null;
        try {
            cis = new CountingInputStream(new BufferedInputStream(new FileInputStream(file)));
            byte[] data = StreamUtils.streamToBytes(cis, (int) (file.length() - cis.bytesRead));
            return readEntry(cis, data);
        } catch (IOException e) {
            WindmillLog.d("%s: %s", file.getAbsolutePath(), e.toString());
            remove(key);
            return null;
        } finally {
            if (cis != null) {
                try {
                    cis.close();
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
    }

    /**
     * Initializes the DiskBasedCache by scanning for all files currently in the
     * specified root directory. Creates the root directory if necessary.
     */
    @Override
    public synchronized void initialize() {
        if (!mRootDirectory.exists()) {
            if (!mRootDirectory.mkdirs()) {
                WindmillLog.e("Unable to create cache dir %s", mRootDirectory.getAbsolutePath());
            }
            return;
        }

        File[] files = mRootDirectory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            CountingInputStream cis = null;
            try {
                cis = new CountingInputStream(new BufferedInputStream(new FileInputStream(file)));
                byte[] data = StreamUtils.streamToBytes(cis, (int) (file.length() - cis.bytesRead));
                CacheEntry cacheEntry = readEntry(cis, data);
                cacheEntry.size = file.length();
                putEntry(cacheEntry.key, cacheEntry);
            } catch (IOException e) {
                if (file != null) {
                    file.delete();
                }
            } finally {
                try {
                    if (cis != null) {
                        cis.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Invalidates an entry in the cache.
     *
     * @param key        Cache key
     * @param fullExpire True to fully expire the entry, false to soft expire
     */
    @Override
    public synchronized void invalidate(String key, boolean fullExpire) {
        CacheEntry cacheEntry = get(key);
        if (cacheEntry != null) {
            cacheEntry.softTtl = 0;
            if (fullExpire) {
                cacheEntry.ttl = 0;
            }
            put(key, cacheEntry);
        }

    }

    /**
     * Puts the cacheEntry with the specified key into the cache.
     */
    @Override
    public synchronized void put(String key, CacheEntry cacheEntry) {
        pruneIfNeeded(cacheEntry.data.length);
        File file = getFileForKey(key);
        try {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
            boolean success = writeEntry(fos, cacheEntry);
            if (!success) {
                fos.close();
                WindmillLog.d("Failed to write header for %s", file.getAbsolutePath());
                throw new IOException();
            }
            fos.write(cacheEntry.data);
            fos.close();
            putEntry(key, cacheEntry);
            return;
        } catch (IOException e) {
        }
        boolean deleted = file.delete();
        if (!deleted) {
            WindmillLog.d("Could not clean up file %s", file.getAbsolutePath());
        }
    }

    /**
     * Removes the specified key from the cache if it exists.
     */
    @Override
    public synchronized void remove(String key) {
        boolean deleted = getFileForKey(key).delete();
        removeEntry(key);
        if (!deleted) {
            WindmillLog.d("Could not delete cache entry for key=%s, filename=%s",
                    key, getFilenameForKey(key));
        }
    }

    /**
     * Creates a pseudo-unique filename for the specified cache key.
     *
     * @param key The key to generate a file name for.
     * @return A pseudo-unique filename.
     */
    private String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }

    /**
     * Returns a file object for the given cache key.
     */
    public File getFileForKey(String key) {
        return new File(mRootDirectory, getFilenameForKey(key));
    }

    /**
     * Prunes the cache to fit the amount of bytes specified.
     *
     * @param neededSpace The amount of bytes we are trying to fit into the cache.
     */
    private void pruneIfNeeded(int neededSpace) {
        if ((mTotalSize + neededSpace) < mMaxCacheSizeInBytes) {
            return;
        }
        if (WindmillLog.DEBUG) {
            WindmillLog.v("Pruning old cache entries.");
        }

        long before = mTotalSize;
        int prunedFiles = 0;
        long startTime = SystemClock.elapsedRealtime();

        Iterator<Map.Entry<String, CacheEntry>> iterator = mEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CacheEntry> entry = iterator.next();
            CacheEntry e = entry.getValue();
            boolean deleted = getFileForKey(e.key).delete();
            if (deleted) {
                mTotalSize -= e.size;
            } else {
                WindmillLog.d("Could not delete cache entry for key=%s, filename=%s",
                        e.key, getFilenameForKey(e.key));
            }
            iterator.remove();
            prunedFiles++;

            if ((mTotalSize + neededSpace) < mMaxCacheSizeInBytes * HYSTERESIS_FACTOR) {
                break;
            }
        }

        if (WindmillLog.DEBUG) {
            WindmillLog.v("pruned %d files, %d bytes, %d ms",
                    prunedFiles, (mTotalSize - before), SystemClock.elapsedRealtime() - startTime);
        }
    }

    /**
     * Puts the cacheEntry with the specified key into the cache.
     *
     * @param key   The key to identify the cacheEntry by.
     * @param cacheEntry The cacheEntry to cache.
     */
    private void putEntry(String key, CacheEntry cacheEntry) {
        if (!mEntries.containsKey(key)) {
            mTotalSize += cacheEntry.size;
        } else {
            CacheEntry oldCacheEntry = mEntries.get(key);
            mTotalSize += (cacheEntry.size - oldCacheEntry.size);
        }
        mEntries.put(key, cacheEntry);
    }

    /**
     * Removes the entry identified by 'key' from the cache.
     */
    private void removeEntry(String key) {
        CacheEntry cacheEntry = mEntries.get(key);
        if (cacheEntry != null) {
            mTotalSize -= cacheEntry.size;
            mEntries.remove(key);
        }
    }

    /**
     * Reads the CacheEntry off of an InputStream and returns a CacheEntry object.
     *
     * @param is The InputStream to read from.
     * @throws IOException
     */
    private CacheEntry readEntry(InputStream is, byte[] data) throws IOException {
        CacheEntry cacheEntry = new CacheEntry();
        cacheEntry.key = StreamUtils.readString(is);
        cacheEntry.data = data;
        cacheEntry.etag = StreamUtils.readString(is);
        if (cacheEntry.etag.equals("")) {
            cacheEntry.etag = null;
        }
        cacheEntry.serverDate = StreamUtils.readLong(is);
        cacheEntry.lastModified = StreamUtils.readLong(is);
        cacheEntry.ttl = StreamUtils.readLong(is);
        cacheEntry.softTtl = StreamUtils.readLong(is);
        cacheEntry.responseHeaders = StreamUtils.readStringStringMap(is);
        return cacheEntry;
    }

    /**
     * Writes the contents of this CacheEntry to the specified OutputStream.
     */
    private boolean writeEntry(OutputStream os, CacheEntry cacheEntry) {
        try {
            StreamUtils.writeString(os, cacheEntry.key);
            StreamUtils.writeString(os, cacheEntry.etag == null ? "" : cacheEntry.etag);
            StreamUtils.writeLong(os, cacheEntry.serverDate);
            StreamUtils.writeLong(os, cacheEntry.lastModified);
            StreamUtils.writeLong(os, cacheEntry.ttl);
            StreamUtils.writeLong(os, cacheEntry.softTtl);
            StreamUtils.writeStringStringMap(cacheEntry.responseHeaders, os);
            os.flush();
            return true;
        } catch (IOException e) {
            WindmillLog.d("%s", e.toString());
            return false;
        }
    }

    private static class CountingInputStream extends FilterInputStream {
        private int bytesRead = 0;

        private CountingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int result = super.read();
            if (result != -1) {
                bytesRead++;
            }
            return result;
        }

        @Override
        public int read(byte[] buffer, int offset, int count) throws IOException {
            int result = super.read(buffer, offset, count);
            if (result != -1) {
                bytesRead += result;
            }
            return result;
        }
    }
}
