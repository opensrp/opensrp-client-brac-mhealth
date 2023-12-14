package org.smartregister.brac.hnpp.utils;

import java.text.SimpleDateFormat;

public class DateFormatUtils {
    private static SimpleDateFormat yyyyMMddSf;
    public static SimpleDateFormat getYyyyMmDdSf(){
        if(yyyyMMddSf == null){
            yyyyMMddSf =  new SimpleDateFormat("yyyy-MM-dd");
        }
        return yyyyMMddSf;
    }
}
