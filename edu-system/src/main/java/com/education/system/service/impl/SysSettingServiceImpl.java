package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.education.system.dto.SettingGroupVO;
import com.education.system.entity.SysSetting;
import com.education.system.mapper.SysSettingMapper;
import com.education.system.service.ISysSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysSettingServiceImpl implements ISysSettingService {

    private final SysSettingMapper settingMapper;

    @Override
    public List<SysSetting> listAll() {
        return settingMapper.selectList(null);
    }

    @Override
    public List<SysSetting> listByGroup(String groupCode) {
        return settingMapper.selectList(
            new LambdaQueryWrapper<SysSetting>()
                .eq(SysSetting::getGroupCode, groupCode)
                .orderByAsc(SysSetting::getId)
        );
    }

    @Override
    public List<SettingGroupVO> listAllGrouped() {
        List<SysSetting> all = settingMapper.selectList(null);
        Map<String, List<SysSetting>> groupedMap = all.stream()
                .collect(Collectors.groupingBy(SysSetting::getGroupCode));
        
        List<SettingGroupVO> result = new ArrayList<>();
        for (Map.Entry<String, List<SysSetting>> entry : groupedMap.entrySet()) {
            SettingGroupVO vo = new SettingGroupVO();
            vo.setGroupCode(entry.getKey());
            
            List<SettingGroupVO.SettingItemVO> items = new ArrayList<>();
            for (SysSetting setting : entry.getValue()) {
                SettingGroupVO.SettingItemVO itemVO = new SettingGroupVO.SettingItemVO();
                itemVO.setSettingKey(setting.getSettingKey());
                itemVO.setSettingValue(setting.getSettingValue());
                items.add(itemVO);
            }
            vo.setItems(items);
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(String groupCode, Map<String, String> kvMap) {
        // 查出该组现有数据
        List<SysSetting> existing = listByGroup(groupCode);
        Map<String, SysSetting> existMap = existing.stream()
            .collect(Collectors.toMap(SysSetting::getSettingKey, s -> s));

        Date now = new Date();
        for (Map.Entry<String, String> entry : kvMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue();
            if (existMap.containsKey(key)) {
                // 更新
                SysSetting setting = existMap.get(key);
                setting.setSettingValue(value);
                setting.setUpdateTime(now);
                settingMapper.updateById(setting);
            } else {
                // 插入
                SysSetting s = new SysSetting();
                s.setGroupCode(groupCode);
                s.setSettingKey(key);
                s.setSettingValue(value);
                s.setCreateTime(now);
                s.setUpdateTime(now);
                settingMapper.insert(s);
            }
        }
    }
}
