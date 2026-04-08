package com.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.course.entity.EduCategory;
import com.education.course.mapper.EduCategoryMapper;
import com.education.course.service.IEduCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程分类Service实现
 */
@Service
public class EduCategoryServiceImpl extends ServiceImpl<EduCategoryMapper, EduCategory> implements IEduCategoryService {

    @Override
    public List<EduCategory> treeList() {
        LambdaQueryWrapper<EduCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(EduCategory::getSortOrder);
        List<EduCategory> allList = list(wrapper);
        return buildTree(allList, 0L);
    }

    /**
     * 构建树形结构（递归）
     */
    private List<EduCategory> buildTree(List<EduCategory> allList, Long parentId) {
        List<EduCategory> result = new ArrayList<>();
        for (EduCategory category : allList) {
            if (parentId.equals(category.getParentId())) {
                List<EduCategory> children = buildTree(allList, category.getId());
                if (!children.isEmpty()) {
                    category.setChildren(children);
                }
                result.add(category);
            }
        }
        return result;
    }
}
