package com.education.system.chain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.exception.BusinessException;
import com.education.system.entity.SysUser;
import com.education.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户存在性校验器（责任链模式）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserExistsValidator extends LoginValidator {

    private final SysUserMapper userMapper;

    @Override
    protected void doValidate(LoginContext context) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, context.getUsername());
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            context.fail("用户不存在");
            return;
        }

        context.setUser(user);
        log.debug("用户存在性校验通过: {}", context.getUsername());
    }
}
