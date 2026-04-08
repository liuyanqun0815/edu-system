package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.common.result.PageResult;
import com.education.system.dto.EvaluationStatsVO;
import com.education.system.entity.SysEvaluation;
import com.education.system.query.EvaluationQuery;

/**
 * 评价服务接口
 */
public interface ISysEvaluationService extends IService<SysEvaluation> {
    
    /**
     * 分页查询评价列表
     */
    PageResult<SysEvaluation> pageList(EvaluationQuery query);
    
    /**
     * 回复评价
     */
    void reply(Long id, String reply);
    
    /**
     * 获取评价统计
     */
    EvaluationStatsVO getStats();
}
