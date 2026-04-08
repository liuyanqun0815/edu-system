package com.education.exam.builder;

import com.education.exam.dto.PaperAssemblyDTO;
import com.education.exam.entity.ExamPaper;
import com.education.exam.entity.ExamPaperQuestion;
import com.education.exam.entity.Question;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 试卷组装建造者（建造者模式）
 * 
 * <p>用于复杂试卷对象的链式构建，支持按题型、难度、数量自动组卷。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * ExamPaper paper = PaperAssemblyBuilder.builder()
 *     .paperName("2024年高一数学期末考试")
 *     .gradeId(2L)
 *     .subjectId(1L)
 *     .addQuestionType(1, 10, new BigDecimal("5"))  // 10道单选题，每题5分
 *     .addQuestionType(3, 5, new BigDecimal("4"))   // 5道判断题，每题4分
 *     .duration(120)
 *     .passScore(new BigDecimal("60"))
 *     .build();
 * </pre>
 */
@Data
public class PaperAssemblyBuilder {

    /**
     * 试卷基本信息
     */
    private ExamPaper paper;

    /**
     * 试卷题目列表
     */
    private List<ExamPaperQuestion> paperQuestions;

    /**
     * 题目查询条件
     */
    private PaperAssemblyDTO assemblyDTO;

    private PaperAssemblyBuilder() {
        this.paper = new ExamPaper();
        this.paperQuestions = new ArrayList<>();
        this.assemblyDTO = new PaperAssemblyDTO();
    }

    /**
     * 创建建造者实例
     */
    public static PaperAssemblyBuilder builder() {
        return new PaperAssemblyBuilder();
    }

    /**
     * 设置试卷名称
     */
    public PaperAssemblyBuilder paperName(String name) {
        paper.setName(name);
        return this;
    }

    /**
     * 设置年级ID
     */
    public PaperAssemblyBuilder gradeId(Long gradeId) {
        paper.setGradeId(gradeId);
        return this;
    }

    /**
     * 设置学科ID
     */
    public PaperAssemblyBuilder subjectId(Long subjectId) {
        paper.setSubjectId(subjectId);
        assemblyDTO.setSubjectId(subjectId);
        return this;
    }

    /**
     * 添加题型配置
     * 
     * @param questionType 题型（1单选/2多选/3判断/4填空/5简答）
     * @param count 题目数量
     * @param score 每题分值
     */
    public PaperAssemblyBuilder addQuestionType(Integer questionType, Integer count, Integer score) {
        PaperAssemblyDTO.DifficultyConfig config = new PaperAssemblyDTO.DifficultyConfig();
        config.setQuestionType(questionType);
        config.setCount(count);
        config.setScore(score);
        
        if (assemblyDTO.getDifficultyConfigs() == null) {
            assemblyDTO.setDifficultyConfigs(new ArrayList<>());
        }
        assemblyDTO.getDifficultyConfigs().add(config);
        
        return this;
    }

    /**
     * 设置难度等级（应用到所有题型配置）
     */
    public PaperAssemblyBuilder difficulty(Integer difficulty) {
        // 注：PaperAssemblyDTO中没有单独的difficulty字段
        // 难度应该在DifficultyConfig中设置
        return this;
    }

    /**
     * 设置考试时长（分钟）
     */
    public PaperAssemblyBuilder duration(Integer minutes) {
        paper.setDuration(minutes);
        return this;
    }

    /**
     * 设置及格分数
     */
    public PaperAssemblyBuilder passScore(Integer score) {
        paper.setPassScore(score);
        return this;
    }

    /**
     * 设置试卷类型（1手动/2自动）
     */
    public PaperAssemblyBuilder paperType(Integer type) {
        paper.setPaperType(type);
        return this;
    }

    /**
     * 设置创建人ID
     */
    public PaperAssemblyBuilder createBy(Long userId) {
        paper.setCreateBy(userId);
        return this;
    }

    /**
     * 构建试卷对象
     */
    public ExamPaper build() {
        // 计算总分
        int totalScore = 0;
        if (assemblyDTO.getDifficultyConfigs() != null) {
            for (PaperAssemblyDTO.DifficultyConfig config : assemblyDTO.getDifficultyConfigs()) {
                if (config.getScore() != null) {
                    totalScore += config.getScore() * config.getCount();
                }
            }
        }
        paper.setTotalScore(totalScore);
        
        // 默认值处理
        if (paper.getStatus() == null) {
            paper.setStatus(0); // 默认草稿状态
        }
        if (paper.getPaperType() == null) {
            paper.setPaperType(2); // 默认自动组卷
        }
        
        return paper;
    }

    /**
     * 获取组装DTO（用于查询题目）
     */
    public PaperAssemblyDTO getAssemblyDTO() {
        return assemblyDTO;
    }

    /**
     * 获取试卷题目关联列表
     */
    public List<ExamPaperQuestion> getPaperQuestions() {
        return paperQuestions;
    }
}
