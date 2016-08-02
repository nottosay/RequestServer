package com.windmill.demo;

import java.util.List;

/**
 * Created by wally.yan on 2016/8/1.
 */

public class CustomerResult {
    public String error;
    public List<Result> results;

    public static  class Result{
        public String _id;
        public String createdAt;
        public String desc;
        public String type;
        public String url;
    }
}
