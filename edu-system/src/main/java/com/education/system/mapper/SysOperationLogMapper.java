package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
