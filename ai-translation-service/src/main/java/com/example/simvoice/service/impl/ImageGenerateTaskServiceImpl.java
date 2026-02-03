package com.example.simvoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.simvoice.entity.ImageGenerateTask;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.mapper.ImageGenerateTaskMapper;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.ImageGenerateTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图片生成任务服务实现类
 */
@Service
@RequiredArgsConstructor
public class ImageGenerateTaskServiceImpl implements ImageGenerateTaskService {
    
    private final ImageGenerateTaskMapper taskMapper;
    
    @Override
    public Long createTask(ImageGenerateTask task) {
        task.setTaskStatus(0); // 初始状态：排队中
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.insert(task);
        return task.getId();
    }
    
    @Override
    public void updateTaskStatus(Long taskId, Integer status, String resultUrl, String errorMessage) {
        ImageGenerateTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "任务不存在");
        }
        
        task.setTaskStatus(status);
        task.setUpdateTime(LocalDateTime.now());
        
        if (status == 2) { // 已完成
            task.setResultUrl(resultUrl);
            task.setFinishTime(LocalDateTime.now());
        } else if (status == 3) { // 失败
            task.setErrorMessage(errorMessage);
            task.setFinishTime(LocalDateTime.now());
        }
        
        taskMapper.updateById(task);
    }
    
    @Override
    public ImageGenerateTask getTaskById(Long taskId) {
        ImageGenerateTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "任务不存在");
        }
        return task;
    }
    
    @Override
    public List<ImageGenerateTask> getUserTasks(String openid, Integer status) {
        LambdaQueryWrapper<ImageGenerateTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImageGenerateTask::getOpenid, openid);
        if (status != null) {
            queryWrapper.eq(ImageGenerateTask::getTaskStatus, status);
        }
        queryWrapper.orderByDesc(ImageGenerateTask::getCreateTime);
        return taskMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<ImageGenerateTask> getPendingTasks() {
        LambdaQueryWrapper<ImageGenerateTask> queryWrapper = new LambdaQueryWrapper<>();
        // 仅查询排队中的任务，由调度任务负责推进状态
        queryWrapper.eq(ImageGenerateTask::getTaskStatus, 0)
                .orderByAsc(ImageGenerateTask::getCreateTime); // 按创建时间升序，先处理早的任务
        return taskMapper.selectList(queryWrapper);
    }
}

