package com.education.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.course.entity.EduChapter;
import com.education.course.mapper.EduChapterMapper;
import com.education.course.service.IEduChapterService;
import org.springframework.stereotype.Service;

/**
 * 课程章节Service实现
 */
@Service
public class EduChapterServiceImpl extends ServiceImpl<EduChapterMapper, EduChapter> implements IEduChapterService {
}
