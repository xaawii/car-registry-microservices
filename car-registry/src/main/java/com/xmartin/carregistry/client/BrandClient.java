package com.xmartin.carregistry.client;


import com.xmartin.carregistry.domain.Brand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "brand-service")
public interface BrandClient {

    @GetMapping("/brands/{id}")
    public Optional<Brand> getBrandById(@PathVariable Integer id);

    @GetMapping("/brands/name/{name}")
    public Optional<Brand> getBrandByName(@PathVariable String name);

    @GetMapping("/brands")
    public List<Brand> getAllBrands();

    @DeleteMapping("/brands/{id}")
    String deleteBrandById(@PathVariable Integer id);

}
