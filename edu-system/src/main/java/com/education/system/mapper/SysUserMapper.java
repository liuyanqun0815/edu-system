package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 重置所有用户在线状态为离线
     */
    @Update("UPDATE sys_user SET is_online = 0 WHERE is_online = 1")
    int resetAllOnlineStatus();
}
