package com.wenxia.swift.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zhouw
 * @date 2022-03-24
 */
@Component
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return ApplicationContextHolder.applicationContext;
    }

    public static String getActiveProfile() {
        String[] activeProfiles = getApplicationContext().getEnvironment().getActiveProfiles();
        return activeProfiles.length == 0 ? "default" : activeProfiles[0];
    }
}
