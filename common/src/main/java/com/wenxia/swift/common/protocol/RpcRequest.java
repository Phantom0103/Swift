package com.wenxia.swift.common.protocol;

import java.io.Serializable;

/**
 * @author zhouw
 * @date 2022-03-15
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 2104861261275175620L;

    private String id;
    private String className;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
