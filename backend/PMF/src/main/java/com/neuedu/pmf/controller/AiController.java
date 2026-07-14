package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * AI 对话接口
     */
    @PostMapping("/chat")
    public ResultData chat(@RequestBody Map<String, Object> params) {
        String question = (String) params.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResultData.fail(400, "问题不能为空");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");
        String reply = aiService.chat(question.trim(), history);
        return ResultData.success(reply);
    }

    /**
     * 主页统计数据：总库存、待办任务数、最后更新时间
     */
    @GetMapping("/homeStats")
    public ResultData homeStats(@RequestParam(value = "assigneeId", required = false) Integer assigneeId) {
        return ResultData.success(aiService.getHomeStats(assigneeId));
    }
}
