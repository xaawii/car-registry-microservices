package com.xmartin.userservice.client;

import com.xmartin.userservice.interceptors.TokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "car-registry", configuration = TokenInterceptor.class)
public interface CarClient {

    @GetMapping("/concessionaire/cars/{id}")
    public String getCar(@PathVariable Integer id);
}
