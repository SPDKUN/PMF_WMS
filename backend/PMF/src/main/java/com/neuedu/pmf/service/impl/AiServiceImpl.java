package com.neuedu.pmf.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.pmf.entity.Batch;
import com.neuedu.pmf.entity.Goods;
import com.neuedu.pmf.entity.Inventory;
import com.neuedu.pmf.entity.OperationLog;
import com.neuedu.pmf.entity.TemperatureHumidityRecord;
import com.neuedu.pmf.entity.Warehouse;
import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.mapper.BatchMapper;
import com.neuedu.pmf.mapper.GoodsMapper;
import com.neuedu.pmf.mapper.InventoryMapper;
import com.neuedu.pmf.mapper.OperationLogMapper;
import com.neuedu.pmf.mapper.TemperatureHumidityRecordMapper;
import com.neuedu.pmf.mapper.WarehouseMapper;
import com.neuedu.pmf.mapper.WorkTaskMapper;
import com.neuedu.pmf.service.AiService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private GoodsMapper goodsMapper;

    @Autowired
    private BatchMapper batchMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private TemperatureHumidityRecordMapper tempHumidityMapper;

    @Autowired
    private OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT =
        "你是一名专业的预制菜仓储管理(WMS)智能助手，严格遵守以下所有规则，不得违反：\n" +
        "1、仅回答与仓储管理相关的问题，包括但不限于：批次管理、出入库、库存盘点、库存调整、质检、次品处理、仓库信息、库位管理、温湿度监控等；\n" +
        "2、绝对拒绝回答娱乐、游戏、金融、政治、个人生活等任何非仓储物流及食品安全类问题；\n" +
        "3、不提供具体的菜谱、烹饪方法或菜品口味评价；不替代专业的食品安全检测或质量认证；\n" +
        "4、对于系统报错、数据异常、设备故障等紧急情况，必须明确提示用户立即联系技术支持或现场主管处理；\n" +
        "5、回答专业准确，逻辑清晰，使用规范的仓储物流术语（如SKU、批次号、先进先出FIFO等），避免模糊不清的表述；\n" +
        "6、如果用户提问无关问题，统一回复：\"抱歉，我仅能提供预制菜仓储管理相关的咨询服务，其他问题无法为您解答。如有WMS系统操作或库存管理疑问，请随时向我提问。\"\n" +
        "7、【严禁编造数据】你绝对不能编造、猜测或虚构任何数字（如库存数量、批次号、任务数量等）。" +
        "如果用户询问具体数据而你无法获取，必须明确告知用户去对应页面查看：" +
        "库存数量→「明细查询-库存明细」；货物列表→「明细查询-货物列表」；" +
        "批次信息→「明细查询-批次列表」；仓库库位→「明细查询-仓库列表」；" +
        "温湿度记录→「明细查询-温湿度记录」；" +
        "待办任务→「工作任务-我的待办」；操作记录→「明细查询-操作日志」。\n" +
        "8、所有回答末尾必须添加免责声明：以上内容仅供仓储作业参考，不能替代标准作业程序(SOP)和专业人员的判断。操作前请务必核对系统数据与实际货物信息。";

    @Override
    public String chat(String userMessage, List<Map<String, String>> conversationHistory) {
        try {
            List<Map<String, Object>> messages = new ArrayList<>();

            // 系统提示词
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            // 注入真实数据库数据作为上下文
            String dataContext = buildDataContext(userMessage);
            if (!dataContext.isEmpty()) {
                Map<String, Object> dataMsg = new HashMap<>();
                dataMsg.put("role", "system");
                dataMsg.put("content", dataContext);
                messages.add(dataMsg);
            }

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

    /**
     * 根据用户问题检测是否需要注入真实数据，返回上下文文本
     */
    private String buildDataContext(String userMessage) {
        StringBuilder ctx = new StringBuilder();
        ctx.append("【以下是当前WMS系统的真实数据，回答时请引用这些数据，不要编造】\n");

        boolean hasData = false;

        // 库存相关：库存、存货、数量、还有多少
        if (containsAny(userMessage, "库存", "存货", "数量", "还有多少", "有多少", "inventory")) {
            ArrayList<Inventory> invList = inventoryMapper.list();
            int totalQty = 0, totalItems = 0;
            Map<String, int[]> goodsSummary = new LinkedHashMap<>(); // goodsName -> [totalQty, count]
            for (Inventory inv : invList) {
                if (inv.getQuantity() != null && inv.getQuantity() > 0) {
                    totalQty += inv.getQuantity();
                    totalItems++;
                    Goods g = goodsMapper.findById(inv.getGoods_id());
                    String name = g != null ? g.getGoods_name() : "货物" + inv.getGoods_id();
                    int[] arr = goodsSummary.computeIfAbsent(name, k -> new int[2]);
                    arr[0] += inv.getQuantity();
                    arr[1]++;
                }
            }
            ctx.append("- 库存总览：共 ").append(totalItems).append(" 条库存记录，总数量 ").append(totalQty).append(" 件\n");
            if (!goodsSummary.isEmpty()) {
                ctx.append("  货物明细：\n");
                for (Map.Entry<String, int[]> e : goodsSummary.entrySet()) {
                    ctx.append("    · ").append(e.getKey()).append("：").append(e.getValue()[0]).append("件（").append(e.getValue()[1]).append("个库位）\n");
                }
            }
            hasData = true;
        }

        // 货物相关
        if (containsAny(userMessage, "货物", "商品", "goods", "有哪些货", "什么货")) {
            ArrayList<Goods> goodsList = goodsMapper.list();
            ctx.append("- 货物列表（共 ").append(goodsList.size()).append(" 种）：\n");
            for (Goods g : goodsList) {
                ctx.append("    · ").append(g.getGoods_name()).append("（编码：").append(g.getGoods_code())
                   .append("，数量：").append(g.getQuantity() != null ? g.getQuantity() : 0)
                   .append(g.getUnit() != null ? g.getUnit() : "").append("）\n");
            }
            hasData = true;
        }

        // 仓库/库位相关
        if (containsAny(userMessage, "仓库", "库位", "库区", "warehouse", "空位", "可用")) {
            ArrayList<Warehouse> whList = warehouseMapper.list();
            ctx.append("- 仓库列表（共 ").append(whList.size()).append(" 个）：\n");
            for (Warehouse wh : whList) {
                ctx.append("    · ").append(wh.getWarehouse_name())
                   .append("（可用：").append(wh.getAvailable_slots() != null ? wh.getAvailable_slots() : 0)
                   .append("/").append(wh.getTotal_slots() != null ? wh.getTotal_slots() : 0)
                   .append("，状态：").append(wh.getStatus()).append("）\n");
            }
            hasData = true;
        }

        // 批次相关
        if (containsAny(userMessage, "批次", "batch", "过期", "到期", "保质期")) {
            ArrayList<Batch> batchList = batchMapper.list();
            ctx.append("- 批次列表（共 ").append(batchList.size()).append(" 个）：\n");
            for (Batch b : batchList) {
                Goods g = goodsMapper.findById(b.getGoods_id());
                ctx.append("    · ").append(b.getBatch_id())
                   .append("（货物：").append(g != null ? g.getGoods_name() : "-")
                   .append("，剩余：").append(b.getRemaining_quantity())
                   .append("，到期：").append(b.getExpiry_date() != null ? b.getExpiry_date().toString() : "-")
                   .append("，状态：").append(b.getBatch_status()).append("）\n");
            }
            hasData = true;
        }

        // 任务相关
        if (containsAny(userMessage, "任务", "待办", "task", "工作")) {
            ArrayList<WorkTask> allTasks = workTaskMapper.list();
            long pendingCount = allTasks.stream().filter(t -> t.getCompleted_time() == null).count();
            ctx.append("- 工作任务总览：共 ").append(allTasks.size()).append(" 个任务，其中 ").append(pendingCount).append(" 个待完成\n");
            hasData = true;
        }

        // 温湿度相关
        if (containsAny(userMessage, "温度", "湿度", "温湿度", "temperature", "humidity")) {
            ArrayList<TemperatureHumidityRecord> records = tempHumidityMapper.list();
            if (!records.isEmpty()) {
                // 按仓库分组，取每个仓库最新的温湿度记录
                Map<Integer, TemperatureHumidityRecord> latestMap = new LinkedHashMap<>();
                for (TemperatureHumidityRecord r : records) {
                    if (r.getRecorded_time() == null) continue;
                    TemperatureHumidityRecord existing = latestMap.get(r.getWarehouse_id());
                    if (existing == null || r.getRecorded_time().isAfter(existing.getRecorded_time())) {
                        latestMap.put(r.getWarehouse_id(), r);
                    }
                }
                ctx.append("- 各仓库最新温湿度记录：\n");
                for (Map.Entry<Integer, TemperatureHumidityRecord> e : latestMap.entrySet()) {
                    Warehouse wh = warehouseMapper.findById(e.getKey());
                    TemperatureHumidityRecord r = e.getValue();
                    ctx.append("    · ").append(wh != null ? wh.getWarehouse_name() : "仓库" + e.getKey())
                       .append("：温度 ").append(r.getTemperature() != null ? r.getTemperature() + "°C" : "-")
                       .append("，湿度 ").append(r.getHumidity() != null ? r.getHumidity() + "%" : "-")
                       .append("（记录时间：").append(r.getRecorded_time().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))).append("）\n");
                }
                hasData = true;
            }
        }

        if (!hasData) return "";
        return ctx.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> getHomeStats(Integer assigneeId) {
        Map<String, Object> stats = new HashMap<>();

        // 总库存数量：只统计正常在库的货物
        // 排除"待入库"（未进仓库）和"待报废"（质检不合格）
        int totalInventory = 0;
        ArrayList<Inventory> allInventory = inventoryMapper.list();
        for (Inventory inv : allInventory) {
            String status = inv.getInventory_status();
            if (!"待入库".equals(status) && !"待报废".equals(status)) {
                totalInventory += (inv.getQuantity() != null ? inv.getQuantity() : 0);
            }
        }
        stats.put("totalInventory", totalInventory);

        // 待办任务数
        int pendingTasks = 0;
        if (assigneeId != null) {
            //通过用户ID找到任务，存入列表中
            ArrayList<WorkTask> tasks = workTaskMapper.findByAssigneeId(assigneeId);
            pendingTasks = (tasks != null ? tasks.size() : 0);
        }
        stats.put("pendingTasks", pendingTasks);

        // 最后更新时间（今天只显示时分，非今天加月/日前缀）
        String lastUpdate = "--";
        //从操作日志列表中找最近更新时间
        ArrayList<OperationLog> logs = operationLogMapper.list();
        if (logs != null && !logs.isEmpty()) {
            //获取第一个，时间一定是最新的
            LocalDateTime latest = logs.get(0).getOperation_time();
            if (latest != null) {
                //同一天，只显示时间
                if (latest.toLocalDate().equals(java.time.LocalDate.now())) {
                    lastUpdate = latest.format(DateTimeFormatter.ofPattern("HH:mm"));
                } else {
                    //最新操作不在今天，显示日期
                    lastUpdate = latest.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
                }
            }
        }
        stats.put("lastUpdate", lastUpdate);

        return stats;
    }
}
