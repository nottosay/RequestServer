package com.windmill.demo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zxs on 16/9/20.
 */

public class ImageInfoResult {
    public String code;
    public String msg;
    public ImageInfo data;
    public static class ImageInfo implements Parcelable {
        public int versionCode;
        public String imageUrl;
        public String jumpUrl;

        public ImageInfo() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.versionCode);
            dest.writeString(this.imageUrl);
            dest.writeString(this.jumpUrl);
        }

        protected ImageInfo(Parcel in) {
            this.versionCode = in.readInt();
            this.imageUrl = in.readString();
            this.jumpUrl = in.readString();
        }

        public static final Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
            @Override
            public ImageInfo createFromParcel(Parcel source) {
                return new ImageInfo(source);
            }

            @Override
            public ImageInfo[] newArray(int size) {
                return new ImageInfo[size];
            }
        };
    }

}
