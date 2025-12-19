package com.example.tool.service;

import com.example.tool.dto.ExpressQueryDTO;
import com.example.tool.vo.ExpressResultVO;
import com.example.tool.vo.ForexVO;
import com.example.tool.vo.LotteryVO;
import com.example.tool.vo.OilPriceVO;

/**
 * 生活查询服务
 */
public interface LifeService {

    ExpressResultVO queryExpress(String openid, ExpressQueryDTO dto);

    OilPriceVO queryOilPrice(String province);

    ForexVO queryForex(String base, String target, java.math.BigDecimal amount);

    LotteryVO queryLottery(String type);
}






