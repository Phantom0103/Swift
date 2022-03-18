package com.wenxia.swift.scan;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhouw
 * @date 2022-03-18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SwiftRpcServiceRegister.class)
public @interface SwiftRpcServiceScan {

    String[] packages();
}
