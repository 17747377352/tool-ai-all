package com.example.simvoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.simvoice.dto.ImeCandidateDTO;
import com.example.simvoice.entity.ImeLatin;
import com.example.simvoice.entity.ImeWord;
import com.example.simvoice.mapper.ImeLatinMapper;
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

    /**
     * 根据拉丁转写输入获取候选词列表
     */
    @Override
    public List<ImeCandidateDTO> candidates(String latin) {
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
                        .orderByDesc(ImeWord::getScore)  // 高分在前
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
        );

        for (ImeWord word : prefixWords) {
            if (seenIds.add(word.getId())) {  // 自动去重
                out.add(toDto(word));
//                if (out.size() >= 9) break;  // 限制总数（输入法通常9个候选）
            }
        }
        return out;
    }

    @Override
    public List<ImeCandidateDTO> select(String latin) {
        // 1. 参数校验
        if (!StringUtils.hasText(latin)) {
            return Collections.emptyList();
        }

        // 2. 查 latin 表获取 suggestion
        ImeLatin latinWord = imeLatinMapper.selectOne(
                new LambdaQueryWrapper<ImeLatin>()
                        .eq(ImeLatin::getLatin, latin)
        );

        if (latinWord == null || !StringUtils.hasText(latinWord.getSuggestion())) {
            return Collections.emptyList();
        }

        // 3. 解析 suggestion 为 ID 列表（格式: "A;B;C;D"）
        String[] ids = latinWord.getSuggestion().split(";");
        if (ids.length == 0) {
            return Collections.emptyList();
        }

        // 4. 批量查询 word 表（避免 N+1）
        List<String> idList = Arrays.asList(ids);
        List<ImeWord> words = imeWordMapper.selectList(
                new LambdaQueryWrapper<ImeWord>()
                        .in(ImeWord::getId, idList)
        );

        if (words.isEmpty()) {
            return Collections.emptyList();
        }
        List<ImeCandidateDTO> out = new ArrayList<>();
//        // 5. 建立 ID -> Word 映射，保持 suggestion 顺序
//        Map<String, ImeWord> wordMap = words.stream()
//                .collect(Collectors.toMap(ImeWord::getId, Function.identity(), (a, b) -> a));
//
//        // 6. 按 suggestion 原始顺序组装结果（latin 表定义的优先级）
//        List<ImeCandidateDTO> out = new ArrayList<>();
//        for (String id : ids) {
//            ImeWord word = wordMap.get(id);
//            if (word != null) {
//                out.add(toDto(word));
//            }
//        }
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


