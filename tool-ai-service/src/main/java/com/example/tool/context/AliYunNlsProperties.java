package com.example.tool.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云智能语音交互配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.nls")
public class AliYunNlsProperties {

    /**
     * 应用 AppKey
     */
    private String appKey;

    /**
     * 访问凭证 AccessKeyId
     */
    private String accessKeyId;

    /**
     * 访问凭证 AccessKeySecret
     */
    private String accessKeySecret;

    /**
     * 网关地址，如：wss://nls-gateway-cn-shanghai.aliyuncs.com/ws/v1
     */
    private String gatewayUrl;

    /**
     * 地域（filetrans 录音文件识别用），默认 cn-shanghai
     */
    private String regionId = "cn-shanghai";

    /**
     * filetrans 产品名称，固定 nls-filetrans
     */
    private String filetransProduct = "nls-filetrans";

    /**
     * filetrans 域名（中国站），如：filetrans.cn-shanghai.aliyuncs.com
     */
    private String filetransDomain = "filetrans.cn-shanghai.aliyuncs.com";

    /**
     * filetrans API 版本（中国站），默认 2018-08-17
     */
    private String filetransApiVersion = "2018-08-17";

    /**
     * task.version，新接入建议 4.0
     */
    private String filetransTaskVersion = "4.0";

    /**
     * 轮询间隔（毫秒）
     */
    private Integer filetransPollIntervalMs = 1000;

    /**
     * 轮询超时（毫秒）
     */
    private Integer filetransPollTimeoutMs = 60000;

    /**
     * 音频格式：pcm/wav/opus
     */
    private String format = "pcm";

    /**
     * 采样率：8000/16000
     */
    private Integer sampleRate = 16000;

    /**
     * 回调接口 URL（用于接收识别结果）
     */
    private String callbackUrl;

    /**
     * 回调接口密钥（用于校验回调请求）
     */
    private String callbackSecret;
}


