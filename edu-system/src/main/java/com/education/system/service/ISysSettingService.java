package com.education.system.service;

import com.education.system.dto.SettingGroupVO;
import com.education.system.entity.SysSetting;

import java.util.List;
import java.util.Map;

public interface ISysSettingService {

    /**
     * 根据分组查询所有设置
     */
    List<SysSetting> listByGroup(String groupCode);

    /**
     * 查询所有设置（按分组聚合）
     */
    List<SettingGroupVO> listAllGrouped();

    /**
     * 批量保存（有则更新，无则插入）
     */
    void saveBatch(String groupCode, Map<String, String> kvMap);
}
