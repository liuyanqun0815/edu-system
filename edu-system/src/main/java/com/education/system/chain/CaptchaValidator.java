package com.education.system.chain;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.education.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 验证码校验器（责任链模式）
 */
@Slf4j
@Component
public class CaptchaValidator extends LoginValidator {

    private static final String CAPTCHA_PREFIX = "captcha:";

    @Override
    protected void doValidate(LoginContext context) {
        // 开发环境可跳过验证码
        String skipCaptcha = System.getenv("SKIP_CAPTCHA");
        if ("true".equalsIgnoreCase(skipCaptcha)) {
            log.debug("开发环境跳过验证码校验");
            return;
        }

        String cachedCode = RedisUtils.get(CAPTCHA_PREFIX + context.getCaptchaKey());
        if (cachedCode == null) {
            context.fail("验证码已过期，请重新获取");
            return;
        }

        if (!cachedCode.equalsIgnoreCase(context.getCaptcha())) {
            context.fail("验证码错误");
            return;
        }

        // 校验成功后删除验证码（一次性使用）
        RedisUtils.delete(CAPTCHA_PREFIX + context.getCaptchaKey());
        log.debug("验证码校验通过");
    }
}
