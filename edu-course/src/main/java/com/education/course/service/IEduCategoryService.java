package com.education.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.course.entity.EduCategory;

import java.util.List;

/**
 * 课程分类Service接口
 */
public interface IEduCategoryService extends IService<EduCategory> {

    /**
     * 查询分类树
     */
    List<EduCategory> treeList();
}
