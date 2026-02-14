package com.example.simvoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.simvoice.dto.ImeCandidateDTO;
import com.example.simvoice.entity.ImeLatin;
import com.example.simvoice.entity.ImePhrase;
import com.example.simvoice.entity.ImeWord;
import com.example.simvoice.mapper.ImeLatinMapper;
import com.example.simvoice.mapper.ImePhraseMapper;
import com.example.simvoice.mapper.ImeWordMapper;
import com.example.simvoice.service.ImeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 蒙文输入法（拉丁转写）服务实现
 * <p>
 * 依赖表：
 * - `word`(id, shape, word, latin, score)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImeServiceImpl implements ImeService {

    private final ImeWordMapper imeWordMapper;
    private final ImeLatinMapper imeLatinMapper;
    private final ImePhraseMapper imePhraseMapper;

    /**
     * 根据拉丁转写输入获取候选词列表
     */
    @Override
    public List<ImeCandidateDTO> candidates(String latin, Integer limit) {
        if (!StringUtils.hasText(latin)) {
            return Collections.emptyList();
        }
        // 使用 LinkedHashSet 保持顺序且去重
        Set<String> seenIds = new LinkedHashSet<>();
        List<ImeCandidateDTO> out = new ArrayList<>();

        // 1. 精确匹配（高分优先）
        List<ImeWord> exactWords = imeWordMapper.selectList(
                new LambdaQueryWrapper<ImeWord>()
                        .eq(ImeWord::getLatin, latin)
                        .orderByDesc(ImeWord::getScore)
                        .last("LIMIT " + limit)// 高分在前

        );
        for (ImeWord word : exactWords) {
            if (seenIds.add(word.getId())) {  // 去重
                out.add(toDto(word));
            }
        }

        // 2. 前缀匹配（补充更多候选）
        List<ImeWord> prefixWords = imeWordMapper.selectList(
                new LambdaQueryWrapper<ImeWord>()
                        .likeRight(ImeWord::getLatin, latin)
                        .orderByDesc(ImeWord::getScore)
                        .last("LIMIT " + limit)
        );
        for (ImeWord word : prefixWords) {
            if (seenIds.add(word.getId())) {  // 自动去重
                out.add(toDto(word));
                if (out.size() >= limit) {
                    break;  // 限制总数（输入法通常9个候选）
                }
            }
        }
        return out;
    }

    @Override
    public List<ImeCandidateDTO> select(String ids, Integer limit) {
        // 1. 参数校验
        if (!StringUtils.hasText(ids)) {
            return Collections.emptyList();
        }
        // 2. 前缀匹配（补充更多候选）
        List<ImePhrase> prefixWords = imePhraseMapper.selectList(
                new LambdaQueryWrapper<ImePhrase>()
                        .likeRight(ImePhrase::getId, ids)
                        .orderByDesc(ImePhrase::getScore)
                        .last("LIMIT " + limit)
        );

        List<String> idList = new ArrayList<>();
        for (ImePhrase prefixWord : prefixWords) {
            String id = prefixWord.getId();
            id = id.replace(ids, "");
            if (id.isEmpty()){
                continue;
            }
            String[] split = id.split(":");
            for (String string : split) {
                if (!string.isEmpty()){
                    idList.add(string);
                }
            }
        }
        if (idList.isEmpty()){
            return Collections.emptyList();
        }
        List<ImeWord> words = imeWordMapper.selectList(
                new LambdaQueryWrapper<ImeWord>()
                        .in(ImeWord::getId, idList)
                        .orderByDesc(ImeWord::getScore)
                        .last("LIMIT " + limit)
        );
        List<ImeCandidateDTO> out = new ArrayList<>();
        for (ImeWord word : words) {
            out.add(toDto(word));
        }
        return out;
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


