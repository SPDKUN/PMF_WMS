package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Location;
import java.util.ArrayList;

public interface LocationService {
    ArrayList<Location> list();

    Location getById(Integer id);

    boolean delete(Integer id);

    boolean save(Location location);

    boolean update(Location location);

    ArrayList<Location> listByZoneId(Integer zoneId);
}
