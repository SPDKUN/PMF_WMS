package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Location;
import com.neuedu.pmf.mapper.LocationMapper;
import com.neuedu.pmf.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationMapper locationMapper;

    @Override
    public ArrayList<Location> list() {
        return locationMapper.list();
    }

    @Override
    public Location getById(Integer id) {
        return locationMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return locationMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(Location location) {
        return locationMapper.save(location) > 0;
    }

    @Override
    public boolean update(Location location) {
        return locationMapper.update(location) > 0;
    }
}
