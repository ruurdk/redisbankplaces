package com.redislabs.demos.redisbankplaces.places;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    private String id;
    private String country;
    private String branch;
    private String name;
    private String address;
    private String phone;

}
