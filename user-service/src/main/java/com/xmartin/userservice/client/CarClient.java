package com.xmartin.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "car-registry")
public interface CarClient {

    @GetMapping("/cars/{id}")
    public String getCar(@PathVariable Integer id);
}
