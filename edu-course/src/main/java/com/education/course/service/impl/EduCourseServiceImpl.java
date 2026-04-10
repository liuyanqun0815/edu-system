package com.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.course.dto.CourseStatsVO;
import com.education.common.exception.BusinessException;
import com.education.common.result.PageResult;
import com.education.course.entity.EduCourse;
import com.education.course.mapper.EduCourseMapper;
import com.education.course.query.CourseQuery;
import com.education.course.service.IEduCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EduCourseServiceImpl extends ServiceImpl<EduCourseMapper, EduCourse> implements IEduCourseService {

    @Override
    public PageResult<EduCourse> pageCourses(CourseQuery query) {
        Page<EduCourse> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (query.getCategoryId() != null) {
            wrapper.eq(EduCourse::getCategoryId, query.getCategoryId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(EduCourse::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(EduCourse::getCourseName, query.getKeyword());
        }
        if (query.getCourseName() != null && !query.getCourseName().isEmpty()) {
            wrapper.like(EduCourse::getCourseName, query.getCourseName());
        }
        
        wrapper.orderByDesc(EduCourse::getCreateTime);
        
        IPage<EduCourse> result = page(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long courseId) {
        EduCourse course = getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        Long count = course.getViewCount() == null ? 0L : course.getViewCount();
        course.setViewCount(count + 1);
        updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementLearnCount(Long courseId) {
        EduCourse course = getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        Long count = course.getLearnCount() == null ? 0L : course.getLearnCount();
        course.setLearnCount(count + 1);
        updateById(course);
        
        log.info("课程[{}]学习人数+1, 当前: {}", courseId, course.getLearnCount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseRating(Long courseId, Double rating) {
        EduCourse course = getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        course.setRating(BigDecimal.valueOf(rating));
        updateById(course);
        
        log.info("课程[{}]评分更新为: {}", courseId, rating);
    }

    @Override
    public com.education.course.dto.CourseStatsVO getCourseStats() {
        com.education.course.dto.CourseStatsVO stats = new com.education.course.dto.CourseStatsVO();
        
        // 总数
        stats.setTotal((long) count());
        
        // 已发布
        LambdaQueryWrapper<EduCourse> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(EduCourse::getStatus, 1);
        stats.setPublished((long) count(publishedWrapper));
        
        // 草稿
        LambdaQueryWrapper<EduCourse> draftWrapper = new LambdaQueryWrapper<>();
        draftWrapper.eq(EduCourse::getStatus, 0);
        stats.setDraft((long) count(draftWrapper));
        
        // 统计数据
        List<EduCourse> allCourses = list();
        long totalViewCount = allCourses.stream()
            .mapToLong(c -> c.getViewCount() == null ? 0L : c.getViewCount())
            .sum();
        stats.setTotalViewCount(totalViewCount);
        
        long totalLearnCount = allCourses.stream()
            .mapToLong(c -> c.getLearnCount() == null ? 0L : c.getLearnCount())
            .sum();
        stats.setTotalLearnCount(totalLearnCount);
        
        BigDecimal avgRating = allCourses.stream()
            .filter(c -> c.getRating() != null)
            .map(EduCourse::getRating)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(allCourses.size(), 1)), 2, BigDecimal.ROUND_HALF_UP);
        stats.setAvgRating(avgRating);
        
        return stats;
    }
}
