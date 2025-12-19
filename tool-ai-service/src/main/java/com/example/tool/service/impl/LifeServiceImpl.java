package com.example.tool.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.tool.dto.ExpressQueryDTO;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.LifeService;
import com.example.tool.vo.ExpressResultVO;
import com.example.tool.vo.ExpressTraceVO;
import com.example.tool.vo.ForexVO;
import com.example.tool.vo.LotteryVO;
import com.example.tool.vo.OilPriceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生活查询服务实现类
 * 提供快递查询、实时油价、汇率换算、彩票开奖等功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
public class LifeServiceImpl implements LifeService {

    // ==================== 配置项 ====================
    
    /**
     * 快递100 API密钥
     */
    @Value("${life.kuaidi100.key:}")
    private String kuaidi100Key;

    /**
     * 快递100 客户编码
     */
    @Value("${life.kuaidi100.customer:}")
    private String kuaidi100Customer;

    /**
     * 是否启用快递100服务
     */
    @Value("${life.kuaidi100.enabled:false}")
    private boolean kuaidi100Enabled;

    /**
     * 聚合数据油价 API 密钥（Juhe）
     * 仅用于国内油价查询接口
     */
    @Value("${juhe.oil.key:}")
    private String juheOilKey;

    /**
     * 是否启用聚合数据油价服务
     */
    @Value("${life.juhe.oil.enabled:false}")
    private boolean juheOilEnabled;

    /**
     * 是否启用汇率服务
     */
    @Value("${life.forex.enabled:false}")
    private boolean forexEnabled;

    /**
     * 是否启用彩票服务
     */
    @Value("${life.lottery.enabled:false}")
    private boolean lotteryEnabled;

    /**
     * 查询快递信息
     * 
     * @param openid 用户openid（用于记录查询日志）
     * @param dto 快递查询请求参数，包含运单号、快递公司、手机号等
     * @return 快递查询结果，包含物流轨迹信息
     * @throws BusinessException 参数错误或查询失败时抛出
     */
    @Override
    public ExpressResultVO queryExpress(String openid, ExpressQueryDTO dto) {
        log.info("开始查询快递信息: openid={}, trackingNo={}, company={}", 
                openid, dto != null ? dto.getTrackingNo() : null, dto != null ? dto.getCompany() : null);
        
        // 参数校验
        if (dto == null || !StringUtils.hasText(dto.getTrackingNo())) {
            log.warn("快递查询参数错误: dto为空或运单号为空");
            throw new BusinessException(ResultCode.PARAM_ERROR, "运单号不能为空");
        }

        // 按快递100官方要求：手机号为必填，且必须为11位完整号码（收/寄件人，只填一个即可）
        if (!StringUtils.hasText(dto.getPhone())) {
            log.warn("快递查询参数错误: 手机号为空，trackingNo={}", dto.getTrackingNo());
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号不能为空，请填写11位收件人或寄件人手机号");
        }
        String phoneRaw = dto.getPhone().trim();
        if (phoneRaw.length() != 11 || !phoneRaw.chars().allMatch(Character::isDigit)) {
            log.warn("快递查询参数错误: 手机号格式不正确，trackingNo={}, phone={}", dto.getTrackingNo(), phoneRaw);
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号格式不正确，请填写11位数字手机号");
        }

        String trackingNo = dto.getTrackingNo().trim();
        String company = StringUtils.hasText(dto.getCompany()) ? dto.getCompany().trim() : null;
        // 使用完整11位手机号传给快递100
        String phone = phoneRaw;

        log.debug("快递查询参数: trackingNo={}, company={}, phone={}", trackingNo, company, phone);

        // 如果启用了快递100服务，使用真实API
        if (kuaidi100Enabled && StrUtil.isNotBlank(kuaidi100Key) && StrUtil.isNotBlank(kuaidi100Customer)) {
            log.info("使用快递100 API查询: trackingNo={}", trackingNo);
            try {
                ExpressResultVO result = queryExpressFromKuaidi100(trackingNo, company, phone);
                log.info("快递100查询成功: trackingNo={}, company={}, tracesCount={}", 
                        trackingNo, result.getCompany(), 
                        result.getTraces() != null ? result.getTraces().size() : 0);
                return result;
            } catch (Exception e) {
                log.error("快递100查询失败，返回示例数据: trackingNo={}, error={}", trackingNo, e.getMessage(), e);
                // 失败时返回示例数据
            }
        } else {
            log.info("快递100服务未启用或配置不完整，返回示例数据: trackingNo={}", trackingNo);
        }

        // 返回示例数据（用于联调或服务未启用时）
        log.debug("返回示例快递数据: trackingNo={}", trackingNo);
        ExpressResultVO vo = new ExpressResultVO();
        vo.setCompany(StringUtils.hasText(company) ? company : "AUTO");
        vo.setTrackingNo(trackingNo);
        vo.setStatus("运输中");
        vo.setTraces(new ArrayList<>());
        vo.getTraces().add(new ExpressTraceVO(
                LocalDateTime.now().minusHours(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "快件到达派送站点"));
        vo.getTraces().add(new ExpressTraceVO(
                LocalDateTime.now().minusHours(4).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "正在派送，联系电话: 188****8888"));
        vo.getTraces().add(new ExpressTraceVO(
                LocalDateTime.now().minusMinutes(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "已签收，感谢使用"));
        return vo;
    }

    /**
     * 通过快递100 API查询快递信息
     * 
     * @param trackingNo 运单号
     * @param company 快递公司编码（可选，为空时自动识别）
     * @param phone 手机号后4位（可选，部分快递需要）
     * @return 快递查询结果
     * @throws BusinessException 查询失败时抛出
     */
    private ExpressResultVO queryExpressFromKuaidi100(String trackingNo, String company, String phone) {
        log.debug("调用快递100 API: trackingNo={}, company={}, phone={}", trackingNo, company, phone);
        
        try {
            // 步骤1: 如果没有指定公司，自动识别快递公司
            if (!StringUtils.hasText(company)) {
                log.debug("开始自动识别快递公司: trackingNo={}", trackingNo);
                String autoUrl = "https://www.kuaidi100.com/autonumber/autoComNum?text=" + trackingNo;
                
                HttpResponse autoResp = HttpRequest.get(autoUrl)
                        .timeout(10000)
                        .execute();
                
                log.debug("快递公司自动识别响应: status={}", autoResp.getStatus());
                
                if (autoResp.getStatus() != 200) {
                    log.error("快递公司自动识别失败: trackingNo={}, status={}", trackingNo, autoResp.getStatus());
                    throw new BusinessException(ResultCode.ERROR, "自动识别快递公司失败");
                }

                JSONObject autoJson = JSON.parseObject(autoResp.body());
                JSONArray autoArray = autoJson.getJSONArray("auto");
                if (autoArray == null || autoArray.isEmpty()) {
                    log.warn("无法识别快递公司: trackingNo={}", trackingNo);
                    throw new BusinessException(ResultCode.ERROR, "无法识别快递公司，请手动指定");
                }
                company = autoArray.getJSONObject(0).getString("comCode");
                log.info("自动识别快递公司成功: trackingNo={}, company={}", trackingNo, company);
            }

            // 步骤2: 构建查询参数并生成签名
            JSONObject paramJson = new JSONObject();
            paramJson.put("com", company);
            paramJson.put("num", trackingNo);
            if (StrUtil.isNotBlank(phone)) {
                paramJson.put("phone", phone);
            }
            // 按官方文档补充推荐参数：行政区域解析 + 高级状态 + 中文 + 倒序 + 快递员信息
            paramJson.put("resultv2", "4");          // 行政解析 + 高级状态
            paramJson.put("show", "0");              // 返回 JSON
            paramJson.put("order", "desc");          // 按时间倒序
            paramJson.put("lang", "zh");             // 中文
            paramJson.put("needCourierInfo", true);  // 尝试提取快递员信息

            String paramStr = paramJson.toJSONString();
            // 签名规则: MD5(param + key + customer) 并转为 32 位大写
            String sign = SecureUtil.md5(paramStr + kuaidi100Key + kuaidi100Customer).toUpperCase();
            log.debug("快递100查询参数构建完成: company={}, param={}", company, paramStr);

            // 步骤3: 调用快递100查询接口
            Map<String, Object> formData = new HashMap<>();
            formData.put("customer", kuaidi100Customer);
            formData.put("param", paramStr);
            formData.put("sign", sign);

            log.debug("开始调用快递100查询接口: trackingNo={}", trackingNo);
            HttpResponse queryResp = HttpRequest.post("https://poll.kuaidi100.com/poll/query.do")
                    .form(formData)
                    .timeout(15000)
                    .execute();

            log.debug("快递100查询接口响应: status={}", queryResp.getStatus());
            
            if (queryResp.getStatus() != 200) {
                log.error("快递100查询接口返回错误状态码: trackingNo={}, status={}", trackingNo, queryResp.getStatus());
                throw new BusinessException(ResultCode.ERROR, "查询失败，状态码: " + queryResp.getStatus());
            }

            // 步骤4: 解析响应结果
            JSONObject resultJson = JSON.parseObject(queryResp.body());
            log.debug("快递100查询结果: trackingNo={}, result={}", trackingNo, resultJson);
            String status = resultJson.getString("status");
            log.debug("快递100查询结果状态: trackingNo={}, status={}", trackingNo, status);
            
            if (!"200".equals(status)) {
                String message = resultJson.getString("message");
                log.warn("快递100查询失败: trackingNo={}, status={}, message={}", trackingNo, status, message);
                throw new BusinessException(ResultCode.ERROR, 
                        StrUtil.isNotBlank(message) ? message : "单号有误或暂无轨迹");
            }

            // 步骤5: 构建返回结果
            ExpressResultVO vo = new ExpressResultVO();
            vo.setCompany(company);
            vo.setTrackingNo(trackingNo);
            vo.setStatus(resultJson.getString("state"));
            
            JSONArray dataArray = resultJson.getJSONArray("data");
            List<ExpressTraceVO> traces = new ArrayList<>();
            if (dataArray != null) {
                log.debug("解析物流轨迹: trackingNo={}, tracesCount={}", trackingNo, dataArray.size());
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject traceObj = dataArray.getJSONObject(i);
                    String time = traceObj.getString("ftime");
                    String context = traceObj.getString("context");
                    traces.add(new ExpressTraceVO(time, context));
                }
            } else {
                log.warn("快递100返回的轨迹数据为空: trackingNo={}", trackingNo);
            }
            vo.setTraces(traces);

            log.info("快递100查询成功: trackingNo={}, company={}, status={}, tracesCount={}", 
                    trackingNo, company, vo.getStatus(), traces.size());
            return vo;
        } catch (BusinessException e) {
            log.error("快递100查询业务异常: trackingNo={}, error={}", trackingNo, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("快递100查询异常: trackingNo={}, company={}", trackingNo, company, e);
            throw new BusinessException(ResultCode.ERROR, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询实时油价
     * 
     * @param province 省份名称（可选，默认为"北京"）
     * @return 油价信息，包含92#、95#、98#、0#柴油价格
     */
    @Override
    public OilPriceVO queryOilPrice(String province) {
        // 参数默认值处理
        if (!StringUtils.hasText(province)) {
            province = "北京";
            log.debug("省份参数为空，使用默认值: 北京");
        }
        
        log.info("开始查询油价: province={}", province);

        // 如果启用了聚合数据油价服务，使用真实API
        if (juheOilEnabled && StrUtil.isNotBlank(juheOilKey)) {
            log.info("使用聚合数据API查询油价: province={}", province);
            try {
                OilPriceVO result = queryOilPriceFromJuhe(province);
                log.info("聚合数据油价查询成功: province={}, price92={}, price95={}", 
                        result.getProvince(), result.getPrice92(), result.getPrice95());
                return result;
            } catch (Exception e) {
                log.error("聚合数据油价查询失败，返回示例数据: province={}, error={}", province, e.getMessage(), e);
                // 失败时返回示例数据
            }
        } else {
            log.info("聚合数据油价服务未启用或配置不完整，返回示例数据: province={}", province);
        }

        // 返回示例数据（用于联调或服务未启用时）
        log.debug("返回示例油价数据: province={}", province);
        OilPriceVO vo = new OilPriceVO();
        vo.setProvince(province);
        vo.setDate(LocalDate.now().toString());
        vo.setPrice92("7.65");
        vo.setPrice95("8.15");
        vo.setPrice98("8.95");
        vo.setPrice0("7.15");
        return vo;
    }

    /**
     * 通过聚合数据API查询油价
     * 
     * @param province 省份名称
     * @return 油价信息
     * @throws BusinessException 查询失败时抛出
     */
    private OilPriceVO queryOilPriceFromJuhe(String province) {
        log.debug("调用聚合数据油价API: province={}", province);
        
        try {
            // 步骤1: 调用聚合数据API获取所有省份的油价数据
            String url = "http://apis.juhe.cn/gnyj/query?key=" + juheOilKey;
            log.debug("聚合数据油价API请求URL: {}", url);
            
            HttpResponse resp = HttpRequest.get(url)
                    .timeout(10000)
                    .execute();

            log.debug("聚合数据油价API响应: status={}", resp.getStatus());
            
            if (resp.getStatus() != 200) {
                log.error("聚合数据油价API返回错误状态码: province={}, status={}", province, resp.getStatus());
                throw new BusinessException(ResultCode.ERROR, "查询失败，状态码: " + resp.getStatus());
            }

            // 步骤2: 解析响应JSON
            JSONObject json = JSON.parseObject(resp.body());
            Integer errorCode = json.getInteger("error_code");
            if (errorCode == null || errorCode != 0) {
                String reason = json.getString("reason");
                log.warn("聚合数据油价API返回错误: province={}, errorCode={}, reason={}", 
                        province, errorCode, reason);
                throw new BusinessException(ResultCode.ERROR, 
                        StrUtil.isNotBlank(reason) ? reason : "查询失败");
            }

            JSONArray resultArray = json.getJSONArray("result");
            if (resultArray == null || resultArray.isEmpty()) {
                log.warn("聚合数据油价API返回数据为空: province={}", province);
                throw new BusinessException(ResultCode.ERROR, "未获取到油价数据");
            }

            log.debug("聚合数据油价API返回省份数量: {}", resultArray.size());

            // 步骤3: 根据省份名称匹配对应的城市数据
            // 支持完整省份名和简称匹配
            String matchedCity = null;
            JSONObject matchedData = null;
            
            // 先尝试精确匹配（支持"北京"、"北京市"、"北京省"等格式）
            log.debug("开始精确匹配省份: province={}", province);
            for (int i = 0; i < resultArray.size(); i++) {
                JSONObject item = resultArray.getJSONObject(i);
                String city = item.getString("city");
                if (province.equals(city) || province.equals(city + "省") || 
                    province.equals(city + "市") || city.contains(province) || 
                    province.contains(city)) {
                    matchedCity = city;
                    matchedData = item;
                    log.debug("精确匹配成功: province={}, matchedCity={}", province, matchedCity);
                    break;
                }
            }
            
            // 如果精确匹配失败，尝试模糊匹配（移除"省"、"市"等后缀）
            if (matchedData == null) {
                log.debug("精确匹配失败，开始模糊匹配: province={}", province);
                for (int i = 0; i < resultArray.size(); i++) {
                    JSONObject item = resultArray.getJSONObject(i);
                    String city = item.getString("city");
                    // 移除"省"、"市"等后缀进行匹配
                    String citySimple = city.replace("省", "").replace("市", "");
                    String provinceSimple = province.replace("省", "").replace("市", "");
                    if (citySimple.equals(provinceSimple) || citySimple.contains(provinceSimple) || 
                        provinceSimple.contains(citySimple)) {
                        matchedCity = city;
                        matchedData = item;
                        log.debug("模糊匹配成功: province={}, matchedCity={}", province, matchedCity);
                        break;
                    }
                }
            }
            
            // 如果还是没匹配到，使用第一个结果（默认数据）
            if (matchedData == null) {
                matchedData = resultArray.getJSONObject(0);
                matchedCity = matchedData.getString("city");
                log.warn("未找到匹配的省份，使用默认数据: province={}, defaultCity={}", province, matchedCity);
            }

            // 步骤4: 构建返回结果
            OilPriceVO vo = new OilPriceVO();
            vo.setProvince(matchedCity);
            vo.setDate(LocalDate.now().toString());
            vo.setPrice92(matchedData.getString("92h") != null ? matchedData.getString("92h") : "0");
            vo.setPrice95(matchedData.getString("95h") != null ? matchedData.getString("95h") : "0");
            vo.setPrice98(matchedData.getString("98h") != null ? matchedData.getString("98h") : "0");
            vo.setPrice0(matchedData.getString("0h") != null ? matchedData.getString("0h") : "0");
            
            log.info("聚合数据油价查询成功: province={}, matchedCity={}, price92={}, price95={}, price98={}, price0={}", 
                    province, matchedCity, vo.getPrice92(), vo.getPrice95(), vo.getPrice98(), vo.getPrice0());
            return vo;
        } catch (BusinessException e) {
            log.error("聚合数据油价查询业务异常: province={}, error={}", province, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("聚合数据油价查询异常: province={}", province, e);
            throw new BusinessException(ResultCode.ERROR, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询汇率换算
     * 
     * @param base 基准币种（默认CNY）
     * @param target 目标币种（默认USD）
     * @param amount 换算金额（可选）
     * @return 汇率信息，包含汇率和换算结果
     */
    @Override
    public ForexVO queryForex(String base, String target, BigDecimal amount) {
        // 参数默认值处理
        String b = StringUtils.hasText(base) ? base.toUpperCase() : "CNY";
        String t = StringUtils.hasText(target) ? target.toUpperCase() : "USD";
        
        log.info("开始查询汇率: base={}, target={}, amount={}", b, t, amount);

        // 如果启用了汇率服务，使用真实API
        if (forexEnabled) {
            log.info("查询汇率: base={}, target={}", b, t);
            try {
                ForexVO result = queryForexFromAPI(b, t, amount);
                log.info("汇率查询成功: base={}, target={}, rate={}, amount={}, converted={}", 
                        result.getBase(), result.getTarget(), result.getRate(), 
                        result.getAmount(), result.getConverted());
                return result;
            } catch (Exception e) {
                log.error("汇率查询失败，返回示例数据: base={}, target={}, error={}", b, t, e.getMessage(), e);
                // 失败时返回示例数据
            }
        } else {
            log.info("汇率服务未启用，返回示例数据: base={}, target={}", b, t);
        }

        // 返回示例数据（用于联调或服务未启用时）
        log.debug("返回示例汇率数据: base={}, target={}", b, t);
        BigDecimal rate = new BigDecimal("0.1400"); // 示例：1 CNY = 0.14 USD
        ForexVO vo = new ForexVO();
        vo.setBase(b);
        vo.setTarget(t);
        vo.setRate(rate);
        vo.setSource("示例数据");
        vo.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (amount != null) {
            vo.setAmount(amount);
            vo.setConverted(amount.multiply(rate).setScale(4, RoundingMode.HALF_UP));
        }
        return vo;
    }

    /**
     * 通过 Exchangerate-API 免费接口查询汇率
     * 文档：https://www.exchangerate-api.com/
     * 接口示例：GET https://api.exchangerate-api.com/v4/latest/USD
     *
     * 特点：
     * - 无需密钥（免费版）
     * - 返回指定基准货币对所有目标货币的汇率
     *
     * @param base   基准币种，例如 CNY / USD
     * @param target 目标币种，例如 USD / EUR
     * @param amount 换算金额（可选）
     * @return 汇率信息
     * @throws BusinessException 查询失败时抛出
     */
    private ForexVO queryForexFromAPI(String base, String target, BigDecimal amount) {
        log.debug("调用 Exchangerate-API 汇率接口: base={}, target={}, amount={}", base, target, amount);

        try {
            // 步骤1：构建请求 URL
            // 直接使用 base 作为基准货币，例如：/latest/CNY
            String url = "https://api.exchangerate-api.com/v4/latest/" + base.toUpperCase();
            log.debug("Exchangerate-API 请求 URL: {}", url);

            // 步骤2：调用 HTTP 接口
            HttpResponse resp = HttpRequest.get(url)
                    .timeout(10000)
                    .execute();

            int status = resp.getStatus();
            log.debug("Exchangerate-API 响应状态码: {}", status);

            if (status != 200) {
                log.error("Exchangerate-API 返回错误状态码: base={}, target={}, status={}", base, target, status);
                throw new BusinessException(ResultCode.ERROR, "汇率查询失败，状态码: " + status);
            }

            String body = resp.body();
            if (StrUtil.isBlank(body)) {
                log.error("Exchangerate-API 返回空响应: base={}, target={}", base, target);
                throw new BusinessException(ResultCode.ERROR, "汇率查询失败，返回空响应");
            }

            log.debug("Exchangerate-API 响应体前 200 字符: {}", body.length() > 200 ? body.substring(0, 200) : body);

            // 步骤3：解析 JSON 响应
            // 典型响应格式：
            // {
            //   "base": "USD",
            //   "date": "2024-01-01",
            //   "rates": { "CNY": 7.12, "EUR": 0.92, ... }
            // }
            JSONObject json = JSON.parseObject(body);
            if (json == null || !json.containsKey("rates")) {
                log.error("Exchangerate-API 响应格式异常: {}", body.length() > 200 ? body.substring(0, 200) : body);
                throw new BusinessException(ResultCode.ERROR, "汇率查询失败，响应格式异常");
            }

            JSONObject rates = json.getJSONObject("rates");
            if (rates == null || !rates.containsKey(target.toUpperCase())) {
                log.error("Exchangerate-API 未找到目标币种汇率: base={}, target={}", base, target);
                throw new BusinessException(ResultCode.ERROR, "未找到目标币种汇率: " + target);
            }

            BigDecimal rate = rates.getBigDecimal(target.toUpperCase());
            if (rate == null) {
                log.error("Exchangerate-API 目标币种汇率为空: base={}, target={}", base, target);
                throw new BusinessException(ResultCode.ERROR, "目标币种汇率为空: " + target);
            }

            log.debug("Exchangerate-API 解析到汇率: base={}, target={}, rate={}", base, target, rate);

            // 步骤4：构建返回结果
            ForexVO vo = new ForexVO();
            vo.setBase(base);
            vo.setTarget(target);
            vo.setRate(rate);
            vo.setSource("Exchangerate-API");
            vo.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                vo.setAmount(amount);
                vo.setConverted(amount.multiply(rate).setScale(4, RoundingMode.HALF_UP));
                log.debug("金额换算: {} {} = {} {}", amount, base, vo.getConverted(), target);
            }

            log.info("Exchangerate-API 汇率查询成功: base={}, target={}, rate={}", base, target, rate);
            return vo;
        } catch (BusinessException e) {
            log.error("汇率查询业务异常: base={}, target={}, error={}", base, target, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("汇率查询异常: base={}, target={}", base, target, e);
            throw new BusinessException(ResultCode.ERROR, "汇率查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询彩票开奖信息
     * 
     * @param type 彩种类型（ssq-双色球，dlt-大乐透，默认为ssq）
     * @return 彩票开奖信息，包含期号、开奖时间、号码等
     */
    @Override
    public LotteryVO queryLottery(String type) {
        // 参数默认值处理
        if (!StringUtils.hasText(type)) {
            type = "ssq";
            log.debug("彩种参数为空，使用默认值: ssq");
        }
        
        log.info("开始查询彩票开奖: type={}", type);

        // 如果启用了彩票服务，使用真实API
        if (lotteryEnabled) {
            log.info("使用500.com API查询彩票开奖: type={}", type);
            try {
                LotteryVO result = queryLotteryFromAPI(type);
                log.info("彩票查询成功: type={}, issue={}, numbers={}", 
                        result.getType(), result.getIssue(), result.getNumbers());
                return result;
            } catch (Exception e) {
                log.error("彩票查询失败，返回示例数据: type={}, error={}", type, e.getMessage(), e);
                // 失败时返回示例数据
            }
        } else {
            log.info("彩票服务未启用，返回示例数据: type={}", type);
        }

        // 返回示例数据（用于联调或服务未启用时）
        log.debug("返回示例彩票数据: type={}", type);
        LotteryVO vo = new LotteryVO();
        vo.setType(type);
        vo.setIssue("2025131");
        vo.setOpenTime(LocalDate.now().toString());
        vo.setNumbers("01 05 08 12 18 33 | 07");
        vo.setDetail("示例数据");
        return vo;
    }

    /**
     * 通过500.com API查询彩票开奖信息
     *
     * @param type 彩种类型（ssq-双色球，dlt-大乐透）
     * @return 彩票开奖信息
     * @throws BusinessException 查询失败时抛出
     */
    private LotteryVO queryLotteryFromAPI(String type) {
        log.debug("调用500.com彩票API: type={}", type);

        try {
            // 步骤1: 转换彩种类型并构建API URL
            // 500.com API参数：gameNo=85(双色球), gameNo=35(大乐透)
            String apiType;
            if ("ssq".equals(type) || "双色球".equals(type)) {
                apiType = "ssq";
            } else if ("dlt".equals(type) || "大乐透".equals(type)) {
                apiType = "dlt";
            } else {
                apiType = "ssq"; // 默认双色球
                log.warn("未知的彩种类型，使用默认值双色球: type={}", type);
            }

            String url;
            if ("dlt".equals(apiType)) {
                // 大乐透：gameNo=35
                url = "https://webapi.sporttery.cn/gateway/lottery/getHistoryPageListV1.qry?gameNo=35&provinceId=0&pageSize=1&isVerify=1&termLimits=1";
            } else {
                // 双色球：gameNo=85
                url = "https://webapi.sporttery.cn/gateway/lottery/getHistoryPageListV1.qry?gameNo=85&provinceId=0&pageSize=1&isVerify=1&termLimits=1";
            }

            log.debug("500.com彩票API请求URL: apiType={}, url={}", apiType, url);

            // 步骤2: 调用500.com API
            HttpResponse resp = HttpRequest.get(url)
                    .header(Header.USER_AGENT,
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                                    "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .header(Header.REFERER, "https://www.lottery.gov.cn/")   // 必须
                    .header(Header.ACCEPT, "application/json, text/plain, */*")
                    .header(Header.ACCEPT_ENCODING, "gzip, deflate, br")
                    .header(Header.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9")
                    // 如果还 567，再把 Cookie 补上（抓浏览器）
                    // .header(Header.COOKIE, "抓到的完整Cookie")
                    .timeout(8000)
                    .execute();

            log.debug("500.com彩票API响应: status={}", resp.getStatus());
            log.debug("500.com彩票API响应内容: {}", resp.body());
            if (resp.getStatus() != 200) {
                log.error("500.com彩票API返回错误状态码: type={}, status={}", type, resp.getStatus());
                throw new BusinessException(ResultCode.ERROR, "查询失败");
            }

            // 步骤3: 解析响应JSON
            JSONObject json = JSON.parseObject(resp.body());
            
            // 检查错误码
            if (!"0".equals(json.getString("errorCode"))) {
                String errorMsg = json.getString("errorMessage");
                log.error("500.com彩票API返回错误: type={}, errorCode={}, errorMessage={}", 
                        type, json.getString("errorCode"), errorMsg);
                throw new BusinessException(ResultCode.ERROR, "查询失败: " + errorMsg);
            }
            
            JSONObject value = json.getJSONObject("value");
            if (value == null) {
                log.warn("500.com彩票API返回数据格式错误: type={}, value字段为空", type);
                throw new BusinessException(ResultCode.ERROR, "未获取到开奖数据");
            }

            JSONArray list = value.getJSONArray("list");
            if (list == null || list.isEmpty()) {
                log.warn("500.com彩票API返回开奖列表为空: type={}", type);
                throw new BusinessException(ResultCode.ERROR, "未获取到开奖数据");
            }

            // 步骤4: 提取最新一期开奖信息
            JSONObject latest = list.getJSONObject(0);
            String issue = latest.getString("lotteryDrawNum");
            String openTime = latest.getString("lotteryDrawTime");
            String lotteryDrawResult = latest.getString("lotteryDrawResult");
            String lotteryGameName = latest.getString("lotteryGameName");

            log.debug("解析开奖信息: type={}, issue={}, openTime={}, result={}, gameName={}",
                    apiType, issue, openTime, lotteryDrawResult, lotteryGameName);

            // 步骤5: 格式化开奖号码
            // API返回的lotteryDrawResult已经是空格分隔的格式，如 "02 05 13 15 28 05 08"
            // 如果包含逗号，则替换为空格；否则直接使用
            String numbers = lotteryDrawResult;
            if (numbers != null && numbers.contains(",")) {
                numbers = numbers.replace(",", " ").replaceAll("\\s+", " ").trim();
            } else if (numbers != null) {
                numbers = numbers.trim();
            } else {
                numbers = "";
            }
            
            if (StrUtil.isBlank(numbers)) {
                log.warn("开奖号码为空: type={}, issue={}", apiType, issue);
                throw new BusinessException(ResultCode.ERROR, "开奖号码为空");
            }
            
            log.debug("格式化后的号码: type={}, numbers={}", apiType, numbers);

            // 步骤6: 构建返回结果
            LotteryVO vo = new LotteryVO();
            vo.setType(apiType);
            vo.setIssue(issue);
            vo.setOpenTime(openTime);
            vo.setNumbers(numbers);
            vo.setDetail((lotteryGameName != null ? lotteryGameName : "彩票") + "最新一期开奖结果");

            log.info("500.com彩票查询成功: type={}, issue={}, numbers={}, gameName={}", 
                    apiType, issue, numbers, lotteryGameName);
            return vo;
        } catch (BusinessException e) {
            log.error("彩票查询业务异常: type={}, error={}", type, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("彩票查询异常: type={}", type, e);
            throw new BusinessException(ResultCode.ERROR, "查询失败: " + e.getMessage());
        }
    }
}

