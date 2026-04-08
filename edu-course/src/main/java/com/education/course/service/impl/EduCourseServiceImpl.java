package com.education.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.course.entity.EduCourse;
import com.education.course.mapper.EduCourseMapper;
import com.education.course.service.IEduCourseService;
import org.springframework.stereotype.Service;

/**
 * 课程Service实现
 */
@Service
public class EduCourseServiceImpl extends ServiceImpl<EduCourseMapper, EduCourse> implements IEduCourseService {
}
