package com.education.system.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户状态校验器（责任链模式）
 */
@Slf4j
@Component
public class UserStatusValidator extends LoginValidator {

    @Override
    protected void doValidate(LoginContext context) {
        if (context.getUser() == null) {
            context.fail("用户信息不存在");
            return;
        }

        Integer status = context.getUser().getStatus();
        if (status == null || status == 0) {
            context.fail("用户已被禁用，请联系管理员");
            return;
        }

        log.debug("用户状态校验通过: {}", context.getUsername());
    }
}
