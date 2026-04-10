package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.WordTrainingRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 单词训练答题记录 Mapper
 */
@Mapper
public interface WordTrainingRecordMapper extends BaseMapper<WordTrainingRecord> {
}
