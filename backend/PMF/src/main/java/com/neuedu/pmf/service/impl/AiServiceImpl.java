package com.neuedu.pmf.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.pmf.entity.Inventory;
import com.neuedu.pmf.entity.OperationLog;
import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.mapper.InventoryMapper;
import com.neuedu.pmf.mapper.OperationLogMapper;
import com.neuedu.pmf.mapper.WorkTaskMapper;
import com.neuedu.pmf.service.AiService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

    @Value("${deepseek.temperature}")
    private double temperature;

    @Value("${deepseek.max-tokens}")
    private int maxTokens;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private WorkTaskMapper workTaskMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT =
        "你是一名专业的预制菜仓储管理(WMS)智能助手，严格遵守以下所有规则，不得违反：\n" +
        "1、仅回答与预制菜新增批次、出入库、库存盘点、库存调整、质检、处理次品等相关问题；\n" +
        "2、绝对拒绝回答娱乐、游戏、金融、政治、个人生活等任何非仓储物流及食品安全类问题；\n" +
        "3、不提供具体的菜谱、烹饪方法或菜品口味评价；不替代专业的食品安全检测或质量认证；\n" +
        "4、对于系统报错、数据异常、设备故障等紧急情况，必须明确提示用户立即联系技术支持或现场主管处理；\n" +
        "5、回答专业准确，逻辑清晰，使用规范的仓储物流术语（如SKU、批次号、先进先出FIFO等），避免模糊不清的表述；\n" +
        "6、如果用户提问无关问题，统一回复：\"抱歉，我仅能提供预制菜仓储管理相关的咨询服务，其他问题无法为您解答。如有WMS系统操作或库存管理疑问，请随时向我提问。\"\n" +
        "7、所有回答末尾必须添加免责声明：以上内容仅供仓储作业参考，不能替代标准作业程序(SOP)和专业人员的判断。操作前请务必核对系统数据与实际货物信息。";

    @Override
    public String chat(String userMessage, List<Map<String, String>> conversationHistory) {
        try {
            List<Map<String, Object>> messages = new ArrayList<>();

            // 系统提示词
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            // 历史对话（限制最近10轮防止token超限）
            if (conversationHistory != null) {
                int start = Math.max(0, conversationHistory.size() - 20);
                for (int i = start; i < conversationHistory.size(); i++) {
                    Map<String, String> his = conversationHistory.get(i);
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("role", his.get("role"));
                    msg.put("content", his.get("content"));
                    messages.add(msg);
                }
            }

            // 用户当前问题
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            String json = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "AI服务暂时不可用，请稍后重试。（HTTP " + response.code() + "）";
                }
                String responseBody = response.body() != null ? response.body().string() : "";
                return extractContent(responseBody);
            }
        } catch (Exception e) {
            System.err.println("AI服务调用失败: " + e.getMessage());
            e.printStackTrace();
            return "AI服务暂时不可用，请稍后重试。（" + e.getClass().getSimpleName() + "）";
        }
    }

    /**
     * 从 DeepSeek API 响应中提取回复内容
     */
    @SuppressWarnings("unchecked")
    private String extractContent(String responseBody) {
        try {
            Map<String, Object> root = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
            return "AI返回了空响应，请重试。";
        } catch (Exception e) {
            return "解析AI响应失败：" + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> getHomeStats(Integer assigneeId) {
        Map<String, Object> stats = new HashMap<>();

        // 总库存数量
        int totalInventory = 0;
        ArrayList<Inventory> allInventory = inventoryMapper.list();
        for (Inventory inv : allInventory) {
            totalInventory += (inv.getQuantity() != null ? inv.getQuantity() : 0);
        }
        stats.put("totalInventory", totalInventory);

        // 待办任务数
        int pendingTasks = 0;
        if (assigneeId != null) {
            ArrayList<WorkTask> tasks = workTaskMapper.findByAssigneeId(assigneeId);
            pendingTasks = (tasks != null ? tasks.size() : 0);
        }
        stats.put("pendingTasks", pendingTasks);

        // 最后更新时间
        String lastUpdate = "--";
        ArrayList<OperationLog> logs = operationLogMapper.list();
        if (logs != null && !logs.isEmpty()) {
            LocalDateTime latest = logs.get(0).getOperation_time();
            if (latest != null) {
                lastUpdate = latest.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        }
        stats.put("lastUpdate", lastUpdate);

        return stats;
    }
}
