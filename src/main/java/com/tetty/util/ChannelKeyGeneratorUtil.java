package com.tetty.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: cfh
 * @Date: 2018/10/2 12:51
 * @Description: 构造保存在ChannelContext中的channel的key值的工具类
 */
public class ChannelKeyGeneratorUtil {
    static final Logger log = LoggerFactory.getLogger(ChannelKeyGeneratorUtil.class);

    public static String generator(String host, int port) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(host).append(":").append(String.valueOf(port));

        log.info("key:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
