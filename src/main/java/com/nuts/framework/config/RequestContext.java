package com.nuts.framework.config;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class RequestContext {
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    public static void unset() {
        RequestContextHolder.resetRequestAttributes();
    }
    
    // Add get method for ResourceInfo if needed, or assume it's stored in request attributes
    public static <T> T get(String key) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return (T) request.getAttribute(key);
        }
        return null;
    }
    
    public static class CurrentContext {
         public static void unset() {
            RequestContextHolder.resetRequestAttributes();
        }
         public static <T> T get(String key) {
            HttpServletRequest request = getRequest();
            if (request != null) {
                return (T) request.getAttribute(key);
            }
            return null;
        }
    }
    
    public static CurrentContext getCurrentContext() {
        return new CurrentContext();
    }
}
