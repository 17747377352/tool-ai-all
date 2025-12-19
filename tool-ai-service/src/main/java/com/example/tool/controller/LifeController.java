package com.example.tool.controller;

import com.example.tool.dto.ExpressQueryDTO;
import com.example.tool.result.Result;
import com.example.tool.service.LifeService;
import com.example.tool.vo.ExpressResultVO;
import com.example.tool.vo.ForexVO;
import com.example.tool.vo.LotteryVO;
import com.example.tool.vo.OilPriceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * 生活查询控制器
 */
@RestController
@RequestMapping("/life")
@RequiredArgsConstructor
public class LifeController {

    private final LifeService lifeService;

    /**
     * 快递查询
     */
    @PostMapping("/express")
    public Result<ExpressResultVO> express(@RequestBody ExpressQueryDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        return Result.success(lifeService.queryExpress(openid, dto));
    }

    /**
     * 实时油价
     */
    @GetMapping("/oil-price")
    public Result<OilPriceVO> oilPrice(@RequestParam(value = "province", required = false) String province) {
        return Result.success(lifeService.queryOilPrice(province));
    }

    /**
     * 汇率换算
     */
    @GetMapping("/forex")
    public Result<ForexVO> forex(@RequestParam(value = "base", required = false) String base, @RequestParam(value = "target", required = false) String target, @RequestParam(value = "amount", required = false) BigDecimal amount) {
        return Result.success(lifeService.queryForex(base, target, amount));
    }

    /**
     * 彩票开奖
     */
    @GetMapping("/lottery")
    public Result<LotteryVO> lottery(@RequestParam(value = "type", required = false) String type) {
        return Result.success(lifeService.queryLottery(type));
    }
}






