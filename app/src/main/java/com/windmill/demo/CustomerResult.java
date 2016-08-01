package com.windmill.demo;

/**
 * Created by wally.yan on 2016/8/1.
 */

public class CustomerResult {
    public String code;
    public String msg;
    public Customer data;

    public class Customer {
        public String customerNo;
        public String customerName;
    }
}
