package com.education.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据脱敏工具类
 * 
 * 用于敏感信息展示时的脱敏处理
 * 
 * @author system
 * @since 2026-04-09
 */
public class DesensitizationUtil {

    /**
     * 手机号脱敏: 138****1234
     */
    public static String mobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return "";
        }
        if (mobile.length() != 11) {
            return mobile;
        }
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 邮箱脱敏: te***@example.com
     */
    public static String email(String email) {
        if (StringUtils.isBlank(email)) {
            return "";
        }
        int index = email.indexOf("@");
        if (index <= 1) {
            return email;
        }
        String prefix = email.substring(0, index);
        String suffix = email.substring(index);
        
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + suffix;
        }
        return prefix.substring(0, 2) + "***" + suffix;
    }

    /**
     * 身份证号脱敏: 110101********1234
     */
    public static String idCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return "";
        }
        if (idCard.length() < 8) {
            return idCard;
        }
        return idCard.replaceAll("(\\d{6})\\d{8}(\\w{4})", "$1********$2");
    }

    /**
     * 姓名脱敏: 张* / 张** / 张***
     */
    public static String name(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + StringUtils.repeat("*", name.length() - 1);
    }

    /**
     * 银行卡号脱敏: 6222 **** **** 1234
     */
    public static String bankCard(String bankCard) {
        if (StringUtils.isBlank(bankCard)) {
            return "";
        }
        if (bankCard.length() < 8) {
            return bankCard;
        }
        return bankCard.replaceAll("(\\d{4})\\d+(\\d{4})", "$1 **** **** $2");
    }

    /**
     * 地址脱敏: 北京市朝阳区****
     */
    public static String address(String address, int reserveLength) {
        if (StringUtils.isBlank(address)) {
            return "";
        }
        if (address.length() <= reserveLength) {
            return address;
        }
        return address.substring(0, reserveLength) + "****";
    }

    /**
     * 密码脱敏: ******
     */
    public static String password(String password) {
        return "******";
    }

    /**
     * 车牌号脱敏: 京A****5
     */
    public static String carNumber(String carNumber) {
        if (StringUtils.isBlank(carNumber)) {
            return "";
        }
        if (carNumber.length() < 7) {
            return carNumber;
        }
        return carNumber.substring(0, 3) + "****" + carNumber.substring(carNumber.length() - 1);
    }
}
