package com.example.simvoice.task;

import com.example.simvoice.entity.ImageGenerateTask;
import com.example.simvoice.service.ImageGenerateTaskService;
import com.example.simvoice.service.impl.ToolServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 图片生成任务调度器
 * 定时扫描任务表，按队列顺序执行生成任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageGenerateTaskScheduler {

    private final ImageGenerateTaskService imageGenerateTaskService;
    private final ToolServiceImpl toolServiceImpl;

    /**
     * 定时扫描待处理任务并执行
     * fixedDelay：上一次执行结束后间隔指定毫秒再次执行
     */
//    @Scheduled(fixedDelay = 5000)// 每5秒执行一次
    @Scheduled(fixedDelay = 20000)// 每20秒执行一次
    public void processPendingTasks() {
        List<ImageGenerateTask> pendingTasks = imageGenerateTaskService.getPendingTasks();
        if (pendingTasks == null || pendingTasks.isEmpty()) {
            return;
        }

        log.info("图片生成调度器：本轮待处理任务数量={}", pendingTasks.size());
        // 简单起见，这里顺序处理所有排队任务
        for (ImageGenerateTask task : pendingTasks) {
            try {
                log.info("图片生成调度器：开始处理任务 taskId={}", task.getId());
                toolServiceImpl.executeImageGenerateTask(task.getId());
            } catch (Exception e) {
                log.error("图片生成调度器：处理任务失败 taskId={}", task.getId(), e);
            }
        }
    }
}


