package com.wenxia.swift.common.protocol;

import java.io.Serializable;

/**
 * @author zhouw
 * @date 2022-03-15
 */
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = -1921327887856337850L;

    private String requestId;
    private int code;
    private String errorMsg;
    private Object data;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
