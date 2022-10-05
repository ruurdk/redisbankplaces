package com.redislabs.demos.redisbankplaces.places;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static redis.clients.jedis.ScanParams.SCAN_POINTER_START;


@RestController
@RequestMapping(path = "/api")
@CrossOrigin
@Slf4j
public class PlaceOverviewController {

    @Autowired
    JedisPool jedis;

    @GetMapping("/places")
    public List<Map> listPlaces() {
        log.info("/places");
        long ts = System.currentTimeMillis();
        List<Map> places = new ArrayList<Map>();

        Jedis rc = jedis.getResource();

        // We use a SCAN query on the keyspace and then access each with HGETALL
        // Certainly we could use RediSearch instead for more sophisticated scans
        ScanParams scanParams = new ScanParams().count(2).match("Places:*");
        String cur = SCAN_POINTER_START;
        do {
            ScanResult<String> scanResult = rc.scan(cur, scanParams);
            List<String> result = scanResult.getResult();
            for (String s : result) {
                places.add(rc.hgetAll(s));
            }
            cur = scanResult.getCursor();
        } while (!cur.equals(SCAN_POINTER_START));

        log.info("Found "+places.size() + " in (ms): "+(System.currentTimeMillis()-ts));

        rc.close(); // ideally should be a try catch on the jedis.getResource()
        return places;
    }

}
