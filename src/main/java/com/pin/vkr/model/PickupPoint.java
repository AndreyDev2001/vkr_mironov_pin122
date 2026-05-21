package com.pin.vkr.model;

import lombok.Data;

@Data
public class PickupPoint {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String workingHours;
}

