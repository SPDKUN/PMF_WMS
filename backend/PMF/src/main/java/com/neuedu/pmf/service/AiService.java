package com.neuedu.pmf.service;

import java.util.Map;

public interface AiService {
    /**
     * 向 DeepSeek AI 发送对话请求
     */
    String chat(String userMessage, java.util.List<Map<String, String>> conversationHistory);

    /**
     * 获取主页统计数据（总库存、待办任务数、最后更新时间）
     */
    Map<String, Object> getHomeStats(Integer assigneeId);
}
