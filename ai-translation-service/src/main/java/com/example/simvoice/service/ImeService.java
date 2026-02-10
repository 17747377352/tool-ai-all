package com.example.simvoice.service;

import com.example.simvoice.dto.ImeCandidateDTO;

import java.util.List;

/**
 * 蒙文输入法（拉丁转写）服务
 */
public interface ImeService {
    /**
     * 根据拉丁输入获取候选词（精确+前缀）
     */
    List<ImeCandidateDTO> candidates(String latin, int limit);

    /**
     * 用户选择候选词，上报用于词频学习（score + 1）
     */
    void select(String wordId);
}


