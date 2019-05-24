package com.cs550.teama.spotflickr.services;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static Map<String, String> getUrlParameters(String url){
        URL realUrl = null;
        try {
            realUrl = new URL(url);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
        if (realUrl != null && realUrl.getQuery() != null){
            return separateParameters(realUrl.getQuery());
        }
        // return nothing when the URL is not really a URL
        return null;
    }

    public static Map<String, String> separateParameters(String params){
        Map<String, String> map = new HashMap<>();
        String[] resArray = params.split("&");
        for (int i=0; i < resArray.length; i++) {
            String currentResponse = resArray[i];
            String[] keyValue = currentResponse.split("=");
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }

    public static String oauthEncode(String input) {
        Map<String, String> oathEncodeMap = new HashMap<>();
        oathEncodeMap.put("\\*", "%2A");
        oathEncodeMap.put("\\+", "%20");
        oathEncodeMap.put("%7E", "~");
        String encoded = "";
        try {
            encoded = URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, String> entry : oathEncodeMap.entrySet()) {
            encoded = encoded.replaceAll(entry.getKey(), entry.getValue());
        }
        return encoded;
    }

    public static String oauthDecode(String input) {
        // currently using only url decoding
        String decoded = "";
        try {
            decoded = URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decoded;
    }

}

