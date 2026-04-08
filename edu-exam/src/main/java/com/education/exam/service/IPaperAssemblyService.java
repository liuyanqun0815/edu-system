package com.education.exam.service;

import com.education.exam.dto.PaperAssemblyDTO;
import com.education.exam.dto.PaperAssemblyResultVO;
import com.education.exam.entity.ExamQuestion;

import java.util.List;

/**
 * 组卷服务接口
 */
public interface IPaperAssemblyService {
    
    /**
     * 按难度随机组卷
     * @param dto 组卷配置
     * @return 组卷结果
     */
    PaperAssemblyResultVO assembleByDifficulty(PaperAssemblyDTO dto);
    
    /**
     * 预览组卷结果（不保存）
     * @param dto 组卷配置
     * @return 组卷预览结果
     */
    PaperAssemblyResultVO previewAssembly(PaperAssemblyDTO dto);
    
    /**
     * 获取某条件下可用题目数量
     * @param subjectId 学科ID
     * @param grade 年级
     * @param difficulty 难度
     * @param questionType 题型
     * @return 可用题目数量
     */
    Integer getAvailableCount(Long subjectId, String grade, Integer difficulty, Integer questionType);
    
    /**
     * 手动选题组卷
     * @param paperId 试卷ID
     * @param questionIds 题目ID列表
     * @param scores 对应分值列表
     * @return 组卷结果
     */
    PaperAssemblyResultVO manualAssembly(Long paperId, List<Long> questionIds, List<Integer> scores);
    
    /**
     * 按知识点组卷（专题训练）
     * @param paperId 试卷ID
     * @param subjectId 学科ID
     * @param grade 年级
     * @param knowledgePoints 知识点列表
     * @param countPerPoint 每个知识点抽取数量
     * @param score 每题分值
     * @return 组卷结果
     */
    PaperAssemblyResultVO assembleByKnowledgePoints(Long paperId, Long subjectId, String grade, 
            List<String> knowledgePoints, Integer countPerPoint, Integer score);
    
    /**
     * 获取某学科的所有知识点
     * @param subjectId 学科ID
     * @return 知识点列表
     */
    List<String> getKnowledgePoints(Long subjectId);
}
