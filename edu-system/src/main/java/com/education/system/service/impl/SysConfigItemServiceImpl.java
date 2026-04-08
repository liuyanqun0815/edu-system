package com.education.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.dto.ConfigCategoryVO;
import com.education.system.entity.SysConfigItem;
import com.education.system.mapper.SysConfigItemMapper;
import com.education.system.service.ISysConfigItemService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置项Service实现
 */
@Service
public class SysConfigItemServiceImpl extends ServiceImpl<SysConfigItemMapper, SysConfigItem> 
        implements ISysConfigItemService {

    @Override
    public List<SysConfigItem> listByCategoryCode(String categoryCode) {
        return baseMapper.selectByCategoryCode(categoryCode);
    }

    @Override
    public List<ConfigCategoryVO> listAllGroupByCategory() {
        List<SysConfigItem> allItems = list();
        Map<String, List<SysConfigItem>> groupedMap = allItems.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 1)
                .collect(Collectors.groupingBy(SysConfigItem::getCategoryCode));
        
        List<ConfigCategoryVO> result = new ArrayList<>();
        for (Map.Entry<String, List<SysConfigItem>> entry : groupedMap.entrySet()) {
            ConfigCategoryVO vo = new ConfigCategoryVO();
            vo.setCategoryCode(entry.getKey());
            vo.setItems(entry.getValue());
            result.add(vo);
        }
        return result;
    }
}
