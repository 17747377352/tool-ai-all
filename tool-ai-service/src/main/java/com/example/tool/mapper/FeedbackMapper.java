package com.example.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tool.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈Mapper
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}

