package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysParent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysParentMapper extends BaseMapper<SysParent> {

    /**
     * 查询某学生的家长列表
     */
    @Select("SELECT p.* FROM sys_parent p " + "INNER JOIN sys_user_parent up ON up.parent_id = p.id "
            + "WHERE up.user_id = #{userId}")
    List<SysParent> selectByStudentId(@Param("userId") Long userId);
}
