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
    public Result<List<ImeCandidateDTO>> candidates(@RequestParam String latin,@RequestParam Integer limit) {
            return Result.success(imeService.candidates(latin, limit));
    }

    /**
     * 联想接口
     * GET /ime/imagine?ids=1:N&limit=9
     */
    @GetMapping("/imagine")
    public Result select(@RequestParam String ids,@RequestParam Integer limit) {
        return Result.success(imeService.select(ids, limit));
    }
}


