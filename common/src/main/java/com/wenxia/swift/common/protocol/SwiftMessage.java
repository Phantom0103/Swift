package com.wenxia.swift.common.protocol;

/**
 * @author zhouw
 * @date 2022-03-17
 */
public class SwiftMessage {

    private int length;
    private byte[] content;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
