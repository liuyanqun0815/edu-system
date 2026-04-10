package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysUserLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录日志Mapper
 */
@Mapper
public interface SysUserLoginLogMapper extends BaseMapper<SysUserLoginLog> {
}
