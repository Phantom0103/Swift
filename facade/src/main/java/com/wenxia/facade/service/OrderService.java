package com.wenxia.facade.service;

import com.wenxia.swift.common.annotation.SwiftRpcService;

/**
 * @author zhouw
 * @date 2022-03-24
 */
@SwiftRpcService(server = "rpc-order-server")
public interface OrderService {

    long count(String merchantCode, String channelKeyword);
}
