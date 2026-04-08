package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysConfigItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 配置项Mapper
 */
@Mapper
public interface SysConfigItemMapper extends BaseMapper<SysConfigItem> {
    
    /**
     * 根据分类编码查询配置项列表
     */
    @Select("SELECT * FROM sys_config_item WHERE category_code = #{categoryCode} AND status = 1 ORDER BY sort")
    List<SysConfigItem> selectByCategoryCode(@Param("categoryCode") String categoryCode);
}
