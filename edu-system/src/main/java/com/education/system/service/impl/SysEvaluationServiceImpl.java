package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.result.PageResult;
import com.education.system.dto.EvaluationStatsVO;
import com.education.system.entity.SysEvaluation;
import com.education.system.mapper.SysEvaluationMapper;
import com.education.system.query.EvaluationQuery;
import com.education.system.service.ISysEvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 评价服务实现
 */
@Service
public class SysEvaluationServiceImpl extends ServiceImpl<SysEvaluationMapper, SysEvaluation> implements ISysEvaluationService {
    
    @Override
    public PageResult<SysEvaluation> pageList(EvaluationQuery query) {
        Page<SysEvaluation> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysEvaluation> wrapper = new LambdaQueryWrapper<>();
        
        // 内容搜索
        if (StringUtils.hasText(query.getContent())) {
            wrapper.and(w -> w.like(SysEvaluation::getContent, query.getContent())
                    .or()
                    .like(SysEvaluation::getUserName, query.getContent()));
        }
        
        // 评分筛选
        if (query.getRating() != null) {
            wrapper.eq(SysEvaluation::getRating, query.getRating());
        }
        
        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(SysEvaluation::getStatus, query.getStatus());
        }
        
        // 评价对象类型筛选
        if (query.getTargetType() != null) {
            wrapper.eq(SysEvaluation::getTargetType, query.getTargetType());
        }
        
        // 评价对象ID筛选
        if (query.getTargetId() != null) {
            wrapper.eq(SysEvaluation::getTargetId, query.getTargetId());
        }
        
        // 按时间倒序
        wrapper.orderByDesc(SysEvaluation::getCreateTime);
        
        page = this.page(page, wrapper);
        return PageResult.of(page);
    }
    
    @Override
    public void reply(Long id, String reply) {
        SysEvaluation evaluation = this.getById(id);
        if (evaluation == null) {
            throw new RuntimeException("评价不存在");
        }
        
        evaluation.setReply(reply);
        evaluation.setStatus(1);
        evaluation.setReplyTime(new Date());
        this.updateById(evaluation);
    }
    
    @Override
    public EvaluationStatsVO getStats() {
        EvaluationStatsVO stats = new EvaluationStatsVO();
        
        // 总评价数
        long total = this.count();
        stats.setTotalCount((int) total);
        
        // 平均评分
        if (total > 0) {
            Double avgRating = this.lambdaQuery()
                    .select(SysEvaluation::getRating)
                    .list()
                    .stream()
                    .mapToInt(SysEvaluation::getRating)
                    .average()
                    .orElse(0.0);
            stats.setAvgRating(avgRating);
        } else {
            stats.setAvgRating(0.0);
        }
        
        // 已回复数
        long replied = this.lambdaQuery()
                .eq(SysEvaluation::getStatus, 1)
                .count();
        stats.setRepliedCount((int) replied);
        
        // 未回复数
        long unreplied = this.lambdaQuery()
                .eq(SysEvaluation::getStatus, 0)
                .count();
        stats.setUnrepliedCount((int) unreplied);
        
        // 好评数（4-5星）
        long good = this.lambdaQuery()
                .ge(SysEvaluation::getRating, 4)
                .count();
        stats.setGoodCount((int) good);
        
        // 中评数（3星）
        long medium = this.lambdaQuery()
                .eq(SysEvaluation::getRating, 3)
                .count();
        stats.setMediumCount((int) medium);
        
        // 差评数（1-2星）
        long bad = this.lambdaQuery()
                .le(SysEvaluation::getRating, 2)
                .count();
        stats.setBadCount((int) bad);
        
        return stats;
    }
}
