package com.example.simvoice.controller;

import com.example.simvoice.dto.ImeCandidateDTO;
import com.example.simvoice.dto.ImeSelectDTO;
import com.example.simvoice.result.Result;
import com.example.simvoice.service.ImeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 输入法接口（蒙文拉丁转写）
 */
@RestController
@RequestMapping("/ime")
@RequiredArgsConstructor
public class ImeController {

    private final ImeService imeService;

    /**
     * 获取候选词
     * GET /ime/candidates?latin=xxx
     */
    @GetMapping("/candidates")
    public Result<List<ImeCandidateDTO>> candidates(@RequestParam String latin) {
        return Result.success(imeService.candidates(latin));
    }

    /**
     * 联想接口
     * POST /ime/imagine?latin=xxx
     */
    @GetMapping("/imagine")
    public Result select(@RequestParam String latin) {
        return Result.success(imeService.select(latin));
    }
}


