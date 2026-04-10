package com.education.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.course.entity.EduChapterProgress;

import java.math.BigDecimal;

/**
 * 章节学习进度Service接口
 */
public interface IEduChapterProgressService extends IService<EduChapterProgress> {

    /**
     * 更新学习进度
     */
    void updateProgress(Long courseId, Long chapterId, Long studentId, BigDecimal progress);

    /**
     * 完成章节学习
     */
    void completeChapter(Long courseId, Long chapterId, Long studentId);

    /**
     * 获取章节学习进度
     */
    EduChapterProgress getProgress(Long courseId, Long chapterId, Long studentId);
}
