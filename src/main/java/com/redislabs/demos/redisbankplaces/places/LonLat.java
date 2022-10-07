package com.redislabs.demos.redisbankplaces.places;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LonLat {
    private String lon;
    private String lat;

    private long responseTime;

    public String toString() {
        return lon+","+lat;
    }

    public static LonLat fromString(String lonlat) {
        String[] ll = lonlat.split(",");
        return new LonLat(ll[0], ll[1], 0);
    }

    public boolean isNaN() {
        return lon == null || lon == "";
    }
}
