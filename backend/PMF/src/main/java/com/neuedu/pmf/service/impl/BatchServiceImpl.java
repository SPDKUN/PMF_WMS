package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Batch;
import com.neuedu.pmf.mapper.BatchMapper;
import com.neuedu.pmf.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
public class BatchServiceImpl implements BatchService {

    @Autowired
    private BatchMapper batchMapper;

    @Override
    public ArrayList<Batch> list() {
        return batchMapper.list();
    }

    @Override
    public Batch getById(String id) {
        return batchMapper.findById(id);
    }

    @Override
    public boolean delete(String id) {
        return batchMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(Batch batch) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "B" + today;

        java.util.List<String> todayIds = batchMapper.findTodayBatchIds(prefix + "%");
        int maxSeq = 0;
        for (String id : todayIds) {
            try {
                int seq = Integer.parseInt(id.substring(10)); // "B20260711" is 9 chars, seq starts at index 9
                if (seq > maxSeq) maxSeq = seq;
            } catch (NumberFormatException ignored) {}
        }
        String batchId = prefix + String.format("%03d", maxSeq + 1);
        batch.setBatch_id(batchId);

        if (batch.getRemaining_quantity() == null) {
            batch.setRemaining_quantity(batch.getInitial_quantity());
        }
        if (batch.getBatch_status() == null) {
            batch.setBatch_status("待检");
        }

        return batchMapper.save(batch) > 0;
    }

    @Override
    public boolean update(Batch batch) {
        return batchMapper.update(batch) > 0;
    }
}
