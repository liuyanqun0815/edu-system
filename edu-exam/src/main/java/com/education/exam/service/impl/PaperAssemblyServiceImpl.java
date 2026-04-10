package com.education.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.exam.dto.PaperAssemblyDTO;
import com.education.exam.dto.PaperAssemblyResultVO;
import com.education.exam.entity.ExamPaper;
import com.education.exam.entity.ExamPaperQuestion;
import com.education.exam.entity.ExamQuestion;
import com.education.exam.mapper.ExamPaperQuestionMapper;
import com.education.exam.service.IExamPaperService;
import com.education.exam.service.IExamQuestionService;
import com.education.exam.service.IPaperAssemblyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组卷服务实现类
 * 
 * <p>提供智能组卷和手动组卷功能，支持按难度、题型、知识点等多维度抽题。</p>
 * 
 * <h3>组卷模式：</h3>
 * <ul>
 *   <li><b>按难度组卷</b>：根据难度配置自动抽题，如：简单题10道、中等题5道、困难题2道</li>
 *   <li><b>按知识点组卷</b>：按知识点分类抽题，覆盖指定知识点</li>
 *   <li><b>手动组卷</b>：教师手动选择题目组成试卷</li>
 * </ul>
 * 
 * <h3>组卷流程：</h3>
 * <pre>
 * 1. 接收组卷参数(PaperAssemblyDTO)
 * 2. 按条件查询候选题目
 * 3. 随机抽取指定数量题目
 * 4. 删除原有试卷-题目关联
 * 5. 保存新的试卷-题目关联（含题目序号和分值）
 * 6. 更新试卷统计信息（题目数、总分）
 * </pre>
 * 
 * <h3>事务处理：</h3>
 * <p>组卷操作使用 @Transactional 保证数据一致性，
 * 删除旧关联和保存新关联在同一事务中，失败时自动回滚。</p>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>题目数量不足时，返回实际可抽取的数量</li>
 *   <li>预览组卷(previewAssembly)不保存数据，仅返回预览结果</li>
 *   <li>同一题目可能被多张试卷使用</li>
 * </ul>
 * 
 * @see IPaperAssemblyService 组卷服务接口
 * @see PaperAssemblyDTO 组卷参数DTO
 * @see PaperAssemblyResultVO 组卷结果VO
 * @author education-team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperAssemblyServiceImpl implements IPaperAssemblyService {

    private final IExamQuestionService questionService;
    private final IExamPaperService paperService;
    private final ExamPaperQuestionMapper paperQuestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaperAssemblyResultVO assembleByDifficulty(PaperAssemblyDTO dto) {
        // 预览组卷结果
        PaperAssemblyResultVO result = previewAssembly(dto);
        
        if (dto.getPaperId() == null) {
            return result;
        }
        
        // 删除原有试卷题目关联
        LambdaQueryWrapper<ExamPaperQuestion> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ExamPaperQuestion::getPaperId, dto.getPaperId());
        paperQuestionMapper.delete(deleteWrapper);
        
        // 保存新的试卷题目关联
        List<ExamQuestion> selectedQuestions = result.getSelectedQuestions();
        if (!CollectionUtils.isEmpty(selectedQuestions)) {
            int order = 1;
            for (ExamQuestion question : selectedQuestions) {
                ExamPaperQuestion paperQuestion = new ExamPaperQuestion();
                paperQuestion.setPaperId(dto.getPaperId());
                paperQuestion.setQuestionId(question.getId());
                paperQuestion.setQuestionOrder(order++);
                // 查找对应配置的分值
                Integer score = findScoreForQuestion(dto.getDifficultyConfigs(), question);
                paperQuestion.setScore(score != null ? score : question.getScore() != null ? question.getScore().intValue() : 10);
                paperQuestionMapper.insert(paperQuestion);
            }
        }
        
        // 更新试卷信息
        ExamPaper paper = new ExamPaper();
        paper.setId(dto.getPaperId());
        paper.setQuestionCount(result.getTotalCount());
        paper.setTotalScore(result.getTotalScore());
        paperService.updateById(paper);
        
        return result;
    }

    @Override
    public PaperAssemblyResultVO previewAssembly(PaperAssemblyDTO dto) {
        PaperAssemblyResultVO result = new PaperAssemblyResultVO();
        result.setPaperId(dto.getPaperId());
        
        List<ExamQuestion> allSelected = new ArrayList<>();
        PaperAssemblyResultVO.DifficultyStats stats = new PaperAssemblyResultVO.DifficultyStats();
        stats.setEasyCount(0);
        stats.setEasyScore(0);
        stats.setMediumCount(0);
        stats.setMediumScore(0);
        stats.setHardCount(0);
        stats.setHardScore(0);
        
        if (CollectionUtils.isEmpty(dto.getDifficultyConfigs())) {
            result.setSelectedQuestions(allSelected);
            result.setTotalCount(0);
            result.setTotalScore(0);
            result.setDifficultyStats(stats);
            return result;
        }
        
        // 按配置随机抽题
        Random random = new Random();
        for (PaperAssemblyDTO.DifficultyConfig config : dto.getDifficultyConfigs()) {
            // 构建查询条件
            LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuestion::getStatus, 1)
                    .eq(config.getDifficulty() != null, ExamQuestion::getDifficulty, config.getDifficulty())
                    .eq(config.getQuestionType() != null, ExamQuestion::getQuestionType, config.getQuestionType())
                    .eq(config.getExamType() != null, ExamQuestion::getExamType, config.getExamType())
                    .eq(dto.getSubjectId() != null, ExamQuestion::getSubjectId, dto.getSubjectId())
                    .eq(dto.getGrade() != null && !dto.getGrade().isEmpty(), ExamQuestion::getGrade, dto.getGrade())
                    .eq(dto.getExamType() != null, ExamQuestion::getExamType, dto.getExamType());
            
            // 查询符合条件的题目
            List<ExamQuestion> candidates = questionService.list(wrapper);
            
            if (CollectionUtils.isEmpty(candidates)) {
                continue;
            }
            
            // 随机抽取指定数量
            int count = Math.min(config.getCount() != null ? config.getCount() : 0, candidates.size());
            Collections.shuffle(candidates, random);
            List<ExamQuestion> selected = candidates.subList(0, count);
            
            // 设置分值
            for (ExamQuestion q : selected) {
                // 临时存储分值（用于前端展示）
                q.setUsageCount(config.getScore() != null ? config.getScore() : 10);
            }
            
            allSelected.addAll(selected);
            
            // 更新统计
            int score = count * (config.getScore() != null ? config.getScore() : 10);
            if (config.getDifficulty() != null) {
                switch (config.getDifficulty()) {
                    case 1:
                        stats.setEasyCount(stats.getEasyCount() + count);
                        stats.setEasyScore(stats.getEasyScore() + score);
                        break;
                    case 2:
                        stats.setMediumCount(stats.getMediumCount() + count);
                        stats.setMediumScore(stats.getMediumScore() + score);
                        break;
                    case 3:
                        stats.setHardCount(stats.getHardCount() + count);
                        stats.setHardScore(stats.getHardScore() + score);
                        break;
                }
            }
        }
        
        result.setSelectedQuestions(allSelected);
        result.setTotalCount(allSelected.size());
        result.setTotalScore(stats.getEasyScore() + stats.getMediumScore() + stats.getHardScore());
        result.setDifficultyStats(stats);
        
        return result;
    }

    @Override
    public Integer getAvailableCount(Long subjectId, String grade, Integer difficulty, Integer questionType) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getStatus, 1)
                .eq(subjectId != null, ExamQuestion::getSubjectId, subjectId)
                .eq(grade != null && !grade.isEmpty(), ExamQuestion::getGrade, grade)
                .eq(difficulty != null, ExamQuestion::getDifficulty, difficulty)
                .eq(questionType != null, ExamQuestion::getQuestionType, questionType);
        
        return questionService.count(wrapper);
    }
    
    /**
     * 获取可用题目数量(支持考试类型)
     */
    public Integer getAvailableCountWithExamType(Long subjectId, String grade, Integer examType, 
                                                  Integer difficulty, Integer questionType) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getStatus, 1)
                .eq(subjectId != null, ExamQuestion::getSubjectId, subjectId)
                .eq(grade != null && !grade.isEmpty(), ExamQuestion::getGrade, grade)
                .eq(examType != null, ExamQuestion::getExamType, examType)
                .eq(difficulty != null, ExamQuestion::getDifficulty, difficulty)
                .eq(questionType != null, ExamQuestion::getQuestionType, questionType);
        
        return questionService.count(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaperAssemblyResultVO manualAssembly(Long paperId, List<Long> questionIds, List<Integer> scores) {
        PaperAssemblyResultVO result = new PaperAssemblyResultVO();
        result.setPaperId(paperId);
        
        if (CollectionUtils.isEmpty(questionIds)) {
            result.setSelectedQuestions(new ArrayList<>());
            result.setTotalCount(0);
            result.setTotalScore(0);
            return result;
        }
        
        // 查询题目
        List<ExamQuestion> questions = questionService.listByIds(questionIds);
        
        // 删除原有试卷题目关联
        LambdaQueryWrapper<ExamPaperQuestion> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ExamPaperQuestion::getPaperId, paperId);
        paperQuestionMapper.delete(deleteWrapper);
        
        // 保存新的试卷题目关联
        int order = 1;
        int totalScore = 0;
        for (int i = 0; i < questionIds.size(); i++) {
            Long qid = questionIds.get(i);
            Integer score = scores != null && i < scores.size() ? scores.get(i) : 10;
            
            ExamPaperQuestion paperQuestion = new ExamPaperQuestion();
            paperQuestion.setPaperId(paperId);
            paperQuestion.setQuestionId(qid);
            paperQuestion.setQuestionOrder(order++);
            paperQuestion.setScore(score);
            paperQuestionMapper.insert(paperQuestion);
            
            totalScore += score;
        }
        
        // 更新试卷信息
        ExamPaper paper = new ExamPaper();
        paper.setId(paperId);
        paper.setQuestionCount(questionIds.size());
        paper.setTotalScore(totalScore);
        paperService.updateById(paper);
        
        result.setSelectedQuestions(questions);
        result.setTotalCount(questionIds.size());
        result.setTotalScore(totalScore);
        
        return result;
    }
    
    /**
     * 查找题目对应的配置分值
     */
    private Integer findScoreForQuestion(List<PaperAssemblyDTO.DifficultyConfig> configs, ExamQuestion question) {
        if (CollectionUtils.isEmpty(configs)) {
            // 如果没有配置，返回题目默认分值
            return question.getScore() != null ? question.getScore().intValue() : 10;
        }
        for (PaperAssemblyDTO.DifficultyConfig config : configs) {
            if (config.getDifficulty() != null && config.getDifficulty().equals(question.getDifficulty())
                    && config.getQuestionType() != null && config.getQuestionType().equals(question.getQuestionType())) {
                return config.getScore();
            }
        }
        // 未匹配到配置时，返回题目默认分值
        return question.getScore() != null ? question.getScore().intValue() : 10;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaperAssemblyResultVO assembleByKnowledgePoints(Long paperId, Long subjectId, String grade,
            List<String> knowledgePoints, Integer countPerPoint, Integer score) {
        PaperAssemblyResultVO result = new PaperAssemblyResultVO();
        result.setPaperId(paperId);
        
        List<ExamQuestion> allSelected = new ArrayList<>();
        PaperAssemblyResultVO.DifficultyStats stats = new PaperAssemblyResultVO.DifficultyStats();
        stats.setEasyCount(0);
        stats.setEasyScore(0);
        stats.setMediumCount(0);
        stats.setMediumScore(0);
        stats.setHardCount(0);
        stats.setHardScore(0);
        
        if (CollectionUtils.isEmpty(knowledgePoints)) {
            result.setSelectedQuestions(allSelected);
            result.setTotalCount(0);
            result.setTotalScore(0);
            result.setDifficultyStats(stats);
            return result;
        }
        
        // 删除原有试卷题目关联
        LambdaQueryWrapper<ExamPaperQuestion> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ExamPaperQuestion::getPaperId, paperId);
        paperQuestionMapper.delete(deleteWrapper);
        
        // 按知识点抽题
        Random random = new Random();
        int order = 1;
        int totalScore = 0;
        
        for (String kp : knowledgePoints) {
            LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuestion::getStatus, 1)
                    .eq(subjectId != null, ExamQuestion::getSubjectId, subjectId)
                    .eq(grade != null && !grade.isEmpty(), ExamQuestion::getGrade, grade)
                    .like(ExamQuestion::getKnowledgePoint, kp);
            
            List<ExamQuestion> candidates = questionService.list(wrapper);
            
            if (CollectionUtils.isEmpty(candidates)) {
                continue;
            }
            
            int count = Math.min(countPerPoint != null ? countPerPoint : 5, candidates.size());
            Collections.shuffle(candidates, random);
            List<ExamQuestion> selected = candidates.subList(0, count);
            
            for (ExamQuestion q : selected) {
                ExamPaperQuestion paperQuestion = new ExamPaperQuestion();
                paperQuestion.setPaperId(paperId);
                paperQuestion.setQuestionId(q.getId());
                paperQuestion.setQuestionOrder(order++);
                paperQuestion.setScore(score != null ? score : 10);
                paperQuestionMapper.insert(paperQuestion);
                
                totalScore += score != null ? score : 10;
                allSelected.add(q);
                
                // 更新难度统计
                if (q.getDifficulty() != null) {
                    switch (q.getDifficulty()) {
                        case 1:
                            stats.setEasyCount(stats.getEasyCount() + 1);
                            stats.setEasyScore(stats.getEasyScore() + (score != null ? score : 10));
                            break;
                        case 2:
                            stats.setMediumCount(stats.getMediumCount() + 1);
                            stats.setMediumScore(stats.getMediumScore() + (score != null ? score : 10));
                            break;
                        case 3:
                            stats.setHardCount(stats.getHardCount() + 1);
                            stats.setHardScore(stats.getHardScore() + (score != null ? score : 10));
                            break;
                    }
                }
            }
        }
        
        // 更新试卷信息
        ExamPaper paper = new ExamPaper();
        paper.setId(paperId);
        paper.setQuestionCount(allSelected.size());
        paper.setTotalScore(totalScore);
        paperService.updateById(paper);
        
        result.setSelectedQuestions(allSelected);
        result.setTotalCount(allSelected.size());
        result.setTotalScore(totalScore);
        result.setDifficultyStats(stats);
        
        return result;
    }

    @Override
    public List<String> getKnowledgePoints(Long subjectId) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getStatus, 1)
                .eq(subjectId != null, ExamQuestion::getSubjectId, subjectId)
                .isNotNull(ExamQuestion::getKnowledgePoint)
                .select(ExamQuestion::getKnowledgePoint);
        
        List<ExamQuestion> questions = questionService.list(wrapper);
        
        // 提取并去重知识点
        return questions.stream()
                .map(ExamQuestion::getKnowledgePoint)
                .filter(kp -> kp != null && !kp.trim().isEmpty())
                .flatMap(kp -> Arrays.stream(kp.split("[,，;；]")))
                .map(String::trim)
                .filter(kp -> !kp.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
