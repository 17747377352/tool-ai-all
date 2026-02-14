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
    List<ImeCandidateDTO> candidates(String latin,Integer  limit);

    /**
     * 联想接口
     */
    List<ImeCandidateDTO>  select(String ids,Integer limit);
}


