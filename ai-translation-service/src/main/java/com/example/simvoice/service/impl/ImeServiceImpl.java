package com.example.simvoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.simvoice.dto.ImeCandidateDTO;
import com.example.simvoice.entity.ImeWord;
import com.example.simvoice.mapper.ImeWordMapper;
import com.example.simvoice.service.ImeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 蒙文输入法（拉丁转写）服务实现
 *
 * 依赖表：
 * - `word`(id, shape, word, latin, score)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImeServiceImpl implements ImeService {

    private final ImeWordMapper imeWordMapper;

    @Override
    public List<ImeCandidateDTO> candidates(String latin, int limit) {
        if (!StringUtils.hasText(latin)) {
            return java.util.Collections.emptyList();
        }
        String q = latin.trim().toLowerCase();
        int finalLimit = Math.min(Math.max(limit, 1), 50);

        // MyBatis-Plus：用前缀匹配 + score 排序
        // 说明：无法直接用 CASE 排精确优先，这里用“先查精确再补前缀”的方式实现相同效果。
        @SuppressWarnings("unchecked")
        List<ImeWord> exact = imeWordMapper.selectList(new LambdaQueryWrapper<ImeWord>()
                .eq(ImeWord::getLatin, q)
                .orderByDesc(ImeWord::getScore)
                .last("LIMIT " + finalLimit));

        int remain = finalLimit - (exact == null ? 0 : exact.size());
        List<ImeWord> prefix = java.util.Collections.emptyList();
        if (remain > 0) {
            @SuppressWarnings("unchecked")
            List<ImeWord> tmp = imeWordMapper.selectList(new LambdaQueryWrapper<ImeWord>()
                    .likeRight(ImeWord::getLatin, q)
                    .ne(ImeWord::getLatin, q)
                    .orderByDesc(ImeWord::getScore)
                    .last("LIMIT " + remain));
            prefix = tmp;
        }

        java.util.List<ImeCandidateDTO> out = new java.util.ArrayList<>(finalLimit);
        if (exact != null) {
            for (ImeWord w : exact) out.add(toDto(w));
        }
        if (prefix != null) {
            for (ImeWord w : prefix) out.add(toDto(w));
        }
        return out;
    }

    @Override
    public void select(String wordId) {
        if (!StringUtils.hasText(wordId)) {
            return;
        }
        ImeWord w = imeWordMapper.selectById(wordId.trim());
        if (w == null) {
            log.warn("IME select: wordId not found or not updated, wordId={}", wordId);
            return;
        }
        Integer score = w.getScore() == null ? 0 : w.getScore();
        w.setScore(score + 1);
        imeWordMapper.updateById(w);
    }

    private static ImeCandidateDTO toDto(ImeWord w) {
        ImeCandidateDTO dto = new ImeCandidateDTO();
        dto.setId(w.getId());
        dto.setWord(w.getWord());
        dto.setShape(w.getShape());
        dto.setScore(w.getScore());
        dto.setLatin(w.getLatin());
        return dto;
    }
}


