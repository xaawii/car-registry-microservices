package com.xmartin.brand_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "car-registry")
public interface CarClient {

    @DeleteMapping("/cars/brand/{brandId}")
    void deleteAllCarsByBrandId(@PathVariable Integer brandId);


}
