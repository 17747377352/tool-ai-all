package com.example.tool.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.example.tool.context.AliYunNlsProperties;
import com.example.tool.dto.RecognizeResult;
import com.example.tool.dto.SubmitTaskResponse;
import com.example.tool.service.OssService;
import com.example.tool.service.AliyunAsrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云智能语音识别实现（录音文件识别 filetrans：异步模式）
 * 
 * 提交任务后立即返回 taskId，后台异步轮询，结果通过回调接口返回
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunAsrServiceImpl implements AliyunAsrService {

    private final AliYunNlsProperties nlsProperties;
    private final OssService ossService;
    private final RestTemplate restTemplate;
    private final TaskExecutor taskExecutor;

    @Override
    public SubmitTaskResponse submitTask(MultipartFile file, String format, Integer sampleRate) {
        if (file == null || file.isEmpty()) {
            return new SubmitTaskResponse(400, "音频文件不能为空", null);
        }

        try {
            // 1) 上传音频到 OSS，拿到公网可访问的 file_link（filetrans 必需）
            String fileLink = uploadToOss(file);

            // 2) 初始化 client
            IAcsClient client = buildClient();

            // 3) SubmitTask 获取 TaskId
            String taskId = submitTaskToAliyun(client, fileLink);

            // 4) 异步轮询结果（submitTask 不等待结果）
            taskExecutor.execute(() -> pollResultTask(taskId));
            log.info("任务提交成功，已触发异步轮询, taskId={}, submitReturnTime={}", taskId, System.currentTimeMillis());
            return new SubmitTaskResponse(200, "任务提交成功", taskId);
        } catch (Exception e) {
            log.error("filetrans submitTask exception", e);
            return new SubmitTaskResponse(500, "任务提交失败: " + e.getMessage(), null);
        }
    }

    /**
     * 上传文件到 OSS，获得公网 URL
     */
    private String uploadToOss(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = "asr/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "/" + UUID.randomUUID() + ext;
        return ossService.uploadFile(file.getInputStream(), fileName, file.getContentType());
    }

    private IAcsClient buildClient() throws ClientException {
        // 参考官方文档：DefaultProfile.addEndpoint + DefaultAcsClient
        DefaultProfile.addEndpoint(
                nlsProperties.getRegionId(),
                nlsProperties.getRegionId(),
                nlsProperties.getFiletransProduct(),
                nlsProperties.getFiletransDomain()
        );
        DefaultProfile profile = DefaultProfile.getProfile(
                nlsProperties.getRegionId(),
                nlsProperties.getAccessKeyId(),
                nlsProperties.getAccessKeySecret()
        );
        return new DefaultAcsClient(profile);
    }

    private String submitTaskToAliyun(IAcsClient client, String fileLink) throws Exception {
        CommonRequest postRequest = new CommonRequest();
        postRequest.setDomain(nlsProperties.getFiletransDomain());
        postRequest.setVersion(nlsProperties.getFiletransApiVersion());
        postRequest.setAction("SubmitTask");
        postRequest.setProduct(nlsProperties.getFiletransProduct());
        postRequest.setMethod(MethodType.POST);
        postRequest.setHttpContentType(FormatType.JSON);

        JSONObject taskObject = new JSONObject();
        taskObject.put("appkey", nlsProperties.getAppKey());
        taskObject.put("file_link", fileLink);
        taskObject.put("version", nlsProperties.getFiletransTaskVersion());

        postRequest.putBodyParameter("Task", taskObject.toJSONString());

        CommonResponse postResponse = client.getCommonResponse(postRequest);
        if (postResponse.getHttpStatus() != 200) {
            throw new RuntimeException("SubmitTask HttpStatus=" + postResponse.getHttpStatus() + ", body=" + postResponse.getData());
        }

        JSONObject resp = JSONObject.parseObject(postResponse.getData());
        log.info(resp.toJSONString());
        String statusText = resp.getString("StatusText");
        if (!"SUCCESS".equals(statusText)) {
            throw new RuntimeException("SubmitTask failed: " + resp.toJSONString());
        }
        return resp.getString("TaskId");
    }

    /**
     * 轮询任务结果（由线程池异步执行）
     * submitTask 不等待该方法完成。
     */
    private void pollResultTask(String taskId) {
        try {
            log.info("开始执行轮询任务, taskId={}, startTime={}", taskId, System.currentTimeMillis());

            IAcsClient client = buildClient();

            CommonRequest getRequest = new CommonRequest();
            getRequest.setDomain(nlsProperties.getFiletransDomain());
            getRequest.setVersion(nlsProperties.getFiletransApiVersion());
            getRequest.setAction("GetTaskResult");
            getRequest.setProduct(nlsProperties.getFiletransProduct());
            getRequest.setMethod(MethodType.GET);
            getRequest.putQueryParameter("TaskId", taskId);

            long start = System.currentTimeMillis();
            int interval = nlsProperties.getFiletransPollIntervalMs() == null ? 1000 : nlsProperties.getFiletransPollIntervalMs();
            int timeout = nlsProperties.getFiletransPollTimeoutMs() == null ? 60000 : nlsProperties.getFiletransPollTimeoutMs();

            while (true) {
                if (System.currentTimeMillis() - start > timeout) {
                    log.error("GetTaskResult timeout, taskId={}", taskId);
                    callCallback(taskId, 500, "识别超时", null);
                    return;
                }

                CommonResponse getResponse = client.getCommonResponse(getRequest);
                if (getResponse.getHttpStatus() != 200) {
                    log.error("GetTaskResult HttpStatus={}, body={}", getResponse.getHttpStatus(), getResponse.getData());
                    callCallback(taskId, 500, "查询失败: " + getResponse.getHttpStatus(), null);
                    return;
                }

                JSONObject resp = JSONObject.parseObject(getResponse.getData());
                log.info("resp:{}",resp.toJSONString());
                String statusText = resp.getString("StatusText");

                if ("RUNNING".equals(statusText) || "QUEUEING".equals(statusText)) {
                    Thread.sleep(interval);
                    continue;
                }

                if ("SUCCESS".equals(statusText) || "SUCCESS_WITH_NO_VALID_FRAGMENT".equals(statusText)) {
                    // 构建 RecognizeResult 并调用回调接口
                    RecognizeResult recognizeResult = buildRecognizeResult(resp);
                    // 你要求：获取到结果后，回调前先延迟 8 秒
                    Thread.sleep(8000);
                    callCallback(taskId, 0, "识别成功", recognizeResult);
                    return;
                }

                // 识别失败
                log.error("GetTaskResult failed, taskId={}, resp={}", taskId, resp.toJSONString());
                callCallback(taskId, 500, "识别失败: " + statusText, null);
                return;
            }
        } catch (Exception e) {
            log.error("pollResultTask exception, taskId={}", taskId, e);
            callCallback(taskId, 500, "轮询异常: " + e.getMessage(), null);
        }
    }

    /**
     * 构建 RecognizeResult 对象
     */
    private RecognizeResult buildRecognizeResult(JSONObject resp) {
        RecognizeResult result = new RecognizeResult();
        result.setCode(0);
        result.setMessage("识别成功");

        RecognizeResult.PayloadDTO payload = new RecognizeResult.PayloadDTO();
        payload.setCode(0);
        payload.setRequestId(resp.getString("TaskId"));

        RecognizeResult.ResultDTO resultDTO = new RecognizeResult.ResultDTO();
        JSONObject resultJson = resp.getJSONObject("Result");
        RecognizeResult.SentenceDTO[] sentenceArray = null;
        
        if (resultJson != null) {
            // 提取 Sentences
            JSONArray sentences = resultJson.getJSONArray("Sentences");
            if (sentences != null && !sentences.isEmpty()) {
                sentenceArray = new RecognizeResult.SentenceDTO[sentences.size()];
                for (int i = 0; i < sentences.size(); i++) {
                    JSONObject s = sentences.getJSONObject(i);
                    RecognizeResult.SentenceDTO sentence = new RecognizeResult.SentenceDTO();
                    sentence.setText(s.getString("Text"));
                    sentence.setBeginTime(s.getLong("BeginTime"));
                    sentence.setEndTime(s.getLong("EndTime"));
                    sentenceArray[i] = sentence;
                }
                resultDTO.setSentences(sentenceArray);
            }

            // 提取 Text
            String text = resultJson.getString("Text");
            if (text == null && sentenceArray != null) {
                // 如果没有 Text，从 Sentences 拼接
                StringBuilder sb = new StringBuilder();
                for (RecognizeResult.SentenceDTO s : sentenceArray) {
                    if (s.getText() != null && !s.getText().trim().isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(s.getText().trim());
                    }
                }
                text = sb.toString();
            }
            resultDTO.setText(text);
        }

        payload.setResult(resultDTO);
        result.setPayload(payload);

        return result;
    }

    /**
     * 调用回调接口
     */
    private void callCallback(String taskId, int code, String message, RecognizeResult recognizeResult) {
        if (nlsProperties.getCallbackUrl() == null || nlsProperties.getCallbackUrl().isEmpty()) {
            log.warn("回调 URL 未配置，跳过回调, taskId={}", taskId);
            return;
        }
        try {
            String url = nlsProperties.getCallbackUrl() + "?audioSpeakerSecret=" + nlsProperties.getCallbackSecret();
            if (recognizeResult == null) {
                // 构建失败结果
                recognizeResult = new RecognizeResult();
                recognizeResult.setCode(code);
                recognizeResult.setMessage(message);
            }
            restTemplate.postForObject(url, recognizeResult, Void.class);
            log.info("回调接口调用成功, taskId={}, code={}", taskId, code);
        } catch (Exception e) {
            log.error("回调接口调用失败, taskId={}", taskId, e);
        }
    }
}
