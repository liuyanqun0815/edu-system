package com.education.system.chain;

import com.education.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 密码校验器（责任链模式）
 */
@Slf4j
@Component
public class PasswordValidator extends LoginValidator {

    @Override
    protected void doValidate(LoginContext context) {
        if (context.getUser() == null) {
            context.fail("用户信息不存在");
            return;
        }

        String encryptedPassword = context.getUser().getPassword();
        if (!SecurityUtils.matchesPassword(context.getPassword(), encryptedPassword)) {
            context.fail("密码错误");
            return;
        }

        log.debug("密码校验通过: {}", context.getUsername());
    }
}
