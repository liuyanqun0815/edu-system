package com.education.system.controller;

import com.education.common.result.RpcResult;
import com.education.system.dto.*;
import com.education.system.service.IWordTrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 单词训练控制器
 */
@Slf4j
@RestController
@RequestMapping("/word/training")
public class WordTrainingController {

    @Autowired
    private IWordTrainingService wordTrainingService;

    /**
     * 开始训练
     */
    @PostMapping("/start")
    public RpcResult<TrainingSessionVO> startTraining(@Validated @RequestBody TrainingConfigDTO config) {
        log.info("开始训练请求: {}", config);
        return wordTrainingService.startTraining(config);
    }

    /**
     * 获取下一题
     */
    @GetMapping("/{sessionId}/next")
    public RpcResult<WordQuestionVO> getNextQuestion(@PathVariable Long sessionId) {
        return wordTrainingService.getNextQuestion(sessionId);
    }

    /**
     * 提交答案
     */
    @PostMapping("/answer")
    public RpcResult<Boolean> submitAnswer(@Validated @RequestBody SubmitAnswerDTO answerDTO) {
        return wordTrainingService.submitAnswer(answerDTO);
    }

    /**
     * 提交训练(结束训练)
     */
    @PostMapping("/{sessionId}/submit")
    public RpcResult<TrainingResultVO> submitTraining(@PathVariable Long sessionId) {
        return wordTrainingService.submitTraining(sessionId);
    }

    /**
     * 放弃训练
     */
    @PostMapping("/{sessionId}/abandon")
    public RpcResult<Void> abandonTraining(@PathVariable Long sessionId) {
        return wordTrainingService.abandonTraining(sessionId);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{sessionId}/detail")
    public RpcResult<TrainingSessionVO> getSessionDetail(@PathVariable Long sessionId) {
        return wordTrainingService.getSessionDetail(sessionId);
    }
}
