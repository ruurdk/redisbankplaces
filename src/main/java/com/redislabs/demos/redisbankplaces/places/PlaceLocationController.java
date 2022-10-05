package com.redislabs.demos.redisbankplaces.places;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin
@Slf4j
public class PlaceLocationController {

    @Autowired
    JedisPool jedis;

    private final static String API = "https://nominatim.openstreetmap.org/search?format=json&q=";


    @GetMapping("/location/{id}")
    public LonLat locatePlace(@PathVariable("id") String id) {
        long ts = System.currentTimeMillis();
        log.info("/location/"+id);

        Jedis rc = jedis.getResource();
        try {
            String addr = rc.hget("Places:"+id, "address");
            LonLat lonlat = lonlatExternalAPI(id, addr, true);
            return lonlat;
        } finally {
            log.info("/location/"+id + " in (ms) "+(System.currentTimeMillis()-ts));
            rc.close();
        }
    }

    /**
     * Call to OpenStreetMap public API
     * to find the long/lat information
     *
     * https://nominatim.openstreetmap.org/search?q=Foppingadreef%2022%201102%20BS%20Amsterdam&format=json
     */
    @SneakyThrows
    private LonLat lonlatExternalAPI(String id, String addr, boolean withCache) {
        String key = "LonLat:"+id;
        LonLat result = new LonLat();

        Jedis rc = jedis.getResource();
        try {
            if (withCache) {
                if (rc.exists(key))
                    return LonLat.fromString(rc.get(key));
            }

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(API + URLEncoder.encode(addr, StandardCharsets.UTF_8));
            getRequest.addHeader("accept", "application/json");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String wsCall = httpClient.execute(getRequest, responseHandler);

            // increment the counter of API call
            rc.incrBy("API:openstreetmap", 1);

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> list = mapper.readValue(wsCall, List.class);
            if (list.size() > 0) {
                result.setLon((String) list.get(0).get("lon"));
                result.setLat((String) list.get(0).get("lat"));
            } else {
                log.error("Place not found - " + addr);
                return result;
            }

            // Set the value into Redis only if the cache is enabled
            if (withCache) {
                rc.set(key, result.toString());
                rc.expire(key, 60);
            }
            return result;
        } finally {
            rc.close();
        }
    }

    @GetMapping("/googlemap/{id}")
    public ResponseEntity<String> googleMapRedirect(@PathVariable("id") String id) {
        LonLat lonlat = locatePlace(id);
        if (lonlat.isNaN()) return ResponseEntity.notFound().build();
        // https://www.google.com/maps/place/<lat>,<lon>/@<lat>,<lon>,<zoom>z
        return ResponseEntity.status(HttpStatus.FOUND).location(
                URI.create("https://www.google.com/maps/place/"+lonlat.getLat()+","+lonlat.getLon()+
                        "/@"+lonlat.getLat()+","+lonlat.getLon()+",16z")).build();
    }

    @GetMapping("/openstreetmap/{id}")
    public ResponseEntity<String> openStreetMapRedirect(@PathVariable("id") String id) {
        LonLat lonlat = locatePlace(id);
        if (lonlat.isNaN()) return ResponseEntity.notFound().build();
        // http://www.openstreetmap.org/?mlat=latitude&mlon=longitude&zoom=12
        return ResponseEntity.status(HttpStatus.FOUND).location(
                URI.create("http://www.openstreetmap.org/?mlat="+lonlat.getLat()+"&mlon="+lonlat.getLon()+
                                "&zoom=16")).build();
    }

    @GetMapping("/openstreetmapembed/{id}")
    public ResponseEntity<String> openStreetMapEmbedRedirect(@PathVariable("id") String id) {
        LonLat lonlat = locatePlace(id);
        if (lonlat.isNaN()) return ResponseEntity.notFound().build();
        // http://www.openstreetmap.org/?mlat=latitude&mlon=longitude&zoom=12
        return ResponseEntity.status(HttpStatus.FOUND).location(
                URI.create(
                        openstreetmap_iframe(Double.valueOf(lonlat.getLon()), Double.valueOf(lonlat.getLat()), 13)
                )).build();
    }

    private double lon2tile(double lon, int zoom) { return (Math.floor((lon+180)/360*Math.pow(2,zoom))); }

    private double lat2tile(double lat, int zoom)  { return (Math.floor((1-Math.log(Math.tan(lat*Math.PI/180) + 1/Math.cos(lat*Math.PI/180))/Math.PI)/2 *Math.pow(2,zoom))); }

    private double rad2deg(double radians) { return radians * (180/Math.PI); }


    private String openstreetmap_iframe(double lon, double lat, int zoom) {
        double xtile = lon2tile(lon, zoom);
        double ytile = lat2tile(lat, zoom);
        double n = Math.pow(2, zoom);
        double lon_deg = xtile / n * 360.0 - 180.0;
        double lat_deg = rad2deg(Math.atan(Math.sinh(Math.PI * (1 - 2 * ytile / n))));

        String urliframe = "https://www.openstreetmap.org/export/embed.html?bbox="+lon+"%2C"+lat+"%2C"+lon_deg+"%2C"+lat_deg+"&marker="+lat+"%2C"+lon+"&layers=ND";
        return urliframe;
    }
    // see https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#PHP




}
