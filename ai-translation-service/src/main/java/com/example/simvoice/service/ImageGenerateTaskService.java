package com.example.simvoice.service;

import com.example.simvoice.entity.ImageGenerateTask;

import java.util.List;

/**
 * 图片生成任务服务接口
 */
public interface ImageGenerateTaskService {
    
    /**
     * 创建生成任务
     * @param task 任务信息
     * @return 任务ID
     */
    Long createTask(ImageGenerateTask task);
    
    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 状态：0-排队中 1-生成中 2-已完成 3-失败
     * @param resultUrl 结果URL（成功时）
     * @param errorMessage 错误信息（失败时）
     */
    void updateTaskStatus(Long taskId, Integer status, String resultUrl, String errorMessage);
    
    /**
     * 根据ID获取任务
     * @param taskId 任务ID
     * @return 任务信息
     */
    ImageGenerateTask getTaskById(Long taskId);
    
    /**
     * 获取用户的任务列表
     * @param openid 用户openid
     * @param status 任务状态（null表示查询所有状态）
     * @return 任务列表
     */
    List<ImageGenerateTask> getUserTasks(String openid, Integer status);
    
    /**
     * 获取待处理的任务（排队中或生成中）
     * @return 任务列表
     */
    List<ImageGenerateTask> getPendingTasks();
}

