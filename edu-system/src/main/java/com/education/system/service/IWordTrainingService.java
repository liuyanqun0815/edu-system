package com.education.system.service;

import com.education.common.result.RpcResult;
import com.education.system.dto.*;

import java.util.List;

/**
 * 单词训练服务接口
 */
public interface IWordTrainingService {

    /**
     * 开始训练
     *
     * @param config 训练配置
     * @return 会话信息
     */
    RpcResult<TrainingSessionVO> startTraining(TrainingConfigDTO config);

    /**
     * 获取下一题
     *
     * @param sessionId 会话ID
     * @return 题目信息
     */
    RpcResult<WordQuestionVO> getNextQuestion(Long sessionId);

    /**
     * 提交答案
     *
     * @param answerDTO 答案DTO
     * @return 是否正确
     */
    RpcResult<Boolean> submitAnswer(SubmitAnswerDTO answerDTO);

    /**
     * 提交训练(结束训练)
     *
     * @param sessionId 会话ID
     * @return 训练结果
     */
    RpcResult<TrainingResultVO> submitTraining(Long sessionId);

    /**
     * 放弃训练
     *
     * @param sessionId 会话ID
     * @return 是否成功
     */
    RpcResult<Void> abandonTraining(Long sessionId);

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    RpcResult<TrainingSessionVO> getSessionDetail(Long sessionId);
}
