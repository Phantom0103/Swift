package com.wenxia.swift.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhouw
 * @date 2022-03-21
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SwiftRpcService {

    /**
     * 远程rpc服务名
     */
    String server();
}
