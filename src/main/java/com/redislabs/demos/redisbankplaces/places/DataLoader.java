package com.redislabs.demos.redisbankplaces.places;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final String LOAD = "src/main/resources/places.json";

    @Autowired
    JedisPool jedis;

    @Autowired
    ResourceLoader resourceLoader;

    @Override
    @SneakyThrows
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //BufferedReader bufferedReader = new BufferedReader(new FileReader(LOAD));
        Resource resource = resourceLoader.getResource("classpath:places.json");
        InputStream inputStream = resource.getInputStream();

        // Serialise to Place Array from disk
        ObjectMapper mapper = new ObjectMapper();
        List<Place> places = Arrays.asList(mapper.readValue(inputStream, Place[].class));
        log.info("Loading {} places.json into Redis", places.size());

        // Batch load into Redis
        Jedis rc = jedis.getResource();
        Pipeline rp = rc.pipelined();
        for (Place p : places) {
            log.info(" Loading into Redis: " + p.getBranch()+ ": "+ p.getName() + " | " + p.getAddress().replace("\n", " "));
            rp.hmset("Places:" + p.getId(),
                    mapper.convertValue(
                            p, Map.class
                    ));
        }
        rp.sync();
        rc.close();
        log.info("Finished loading data into Redis");
    }
}
