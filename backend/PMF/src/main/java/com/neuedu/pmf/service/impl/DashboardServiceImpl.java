package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Goods;
import com.neuedu.pmf.entity.Warehouse;
import com.neuedu.pmf.mapper.DashboardMapper;
import com.neuedu.pmf.mapper.GoodsMapper;
import com.neuedu.pmf.mapper.WarehouseMapper;
import com.neuedu.pmf.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public Map<String, Object> getWeeklyInboundOutbound() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(6);

        List<Map<String, Object>> inboundRows = dashboardMapper.weeklyInbound(sevenDaysAgo);
        List<Map<String, Object>> outboundRows = dashboardMapper.weeklyOutbound(sevenDaysAgo);

        // Build a map keyed by date string
        Map<LocalDate, Integer> inboundMap = new LinkedHashMap<>();
        Map<LocalDate, Integer> outboundMap = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate d = sevenDaysAgo.plusDays(i);
            inboundMap.put(d, 0);
            outboundMap.put(d, 0);
        }

        for (Map<String, Object> row : inboundRows) {
            Object dateObj = row.get("record_date");
            Object qtyObj = row.get("total_quantity");
            if (dateObj != null && qtyObj != null) {
                LocalDate d = dateObj instanceof java.sql.Date ? ((java.sql.Date) dateObj).toLocalDate() : LocalDate.parse(dateObj.toString());
                inboundMap.put(d, ((Number) qtyObj).intValue());
            }
        }

        for (Map<String, Object> row : outboundRows) {
            Object dateObj = row.get("record_date");
            Object qtyObj = row.get("total_quantity");
            if (dateObj != null && qtyObj != null) {
                LocalDate d = dateObj instanceof java.sql.Date ? ((java.sql.Date) dateObj).toLocalDate() : LocalDate.parse(dateObj.toString());
                outboundMap.put(d, ((Number) qtyObj).intValue());
            }
        }

        List<String> dates = new ArrayList<>();
        List<Integer> inbound = new ArrayList<>();
        List<Integer> outbound = new ArrayList<>();

        for (LocalDate d : inboundMap.keySet()) {
            dates.add(d.toString());
            inbound.add(inboundMap.get(d));
            outbound.add(outboundMap.get(d));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("inbound", inbound);
        result.put("outbound", outbound);
        return result;
    }

    @Override
    public Map<String, Object> getGoodsQuantity() {
        List<Goods> goodsList = goodsMapper.list();
        List<String> names = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (Goods g : goodsList) {
            names.add(g.getGoods_name());
            quantities.add(g.getQuantity() != null ? g.getQuantity() : 0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("names", names);
        result.put("quantities", quantities);
        return result;
    }

    @Override
    public Map<String, Object> getWarehouseTempHumidity() {
        List<Map<String, Object>> rows = dashboardMapper.latestTempHumidityPerWarehouse();

        List<String> names = new ArrayList<>();
        List<Double> temperatures = new ArrayList<>();
        List<Double> humidities = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            names.add((String) row.get("warehouse_name"));
            Object temp = row.get("temperature");
            Object hum = row.get("humidity");
            temperatures.add(temp != null ? ((Number) temp).doubleValue() : 0);
            humidities.add(hum != null ? ((Number) hum).doubleValue() : 0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("names", names);
        result.put("temperatures", temperatures);
        result.put("humidities", humidities);
        return result;
    }

    @Override
    public Map<String, Object> getWarehouseCapacity() {
        List<Warehouse> warehouses = warehouseMapper.list();
        List<Map<String, Object>> list = new ArrayList<>();

        for (Warehouse wh : warehouses) {
            int total = wh.getTotal_slots() != null ? wh.getTotal_slots() : 0;
            int available = wh.getAvailable_slots() != null ? wh.getAvailable_slots() : 0;
            int used = total - available;
            double saturation = total > 0 ? (double) used / total * 100 : 0;
            saturation = new BigDecimal(saturation).setScale(1, RoundingMode.HALF_UP).doubleValue();

            Map<String, Object> item = new HashMap<>();
            item.put("warehouseName", wh.getWarehouse_name());
            item.put("totalSlots", total);
            item.put("usedSlots", used);
            item.put("availableSlots", available);
            item.put("saturation", saturation);
            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("warehouses", list);
        return result;
    }
}
