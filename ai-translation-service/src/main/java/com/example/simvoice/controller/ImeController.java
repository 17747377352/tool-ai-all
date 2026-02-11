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
     * GET /ime/candidates?latin=xxx&limit=9
     */
    @GetMapping("/candidates")
    public Result<List<ImeCandidateDTO>> candidates(@RequestParam String latin,
                                                    @RequestParam(defaultValue = "9") int limit) {
        return Result.success(imeService.candidates(latin, limit));
    }

    /**
     * 上报用户选择（词频学习）
     * POST /ime/select { "wordId": "..." }
     */
    @PostMapping("/select")
    public Result<Void> select(@RequestBody ImeSelectDTO dto) {
        if (dto != null) {
            imeService.select(dto.getWordId());
        }
        return Result.success();
    }
}


