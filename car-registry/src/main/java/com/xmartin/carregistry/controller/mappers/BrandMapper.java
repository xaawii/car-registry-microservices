package com.xmartin.carregistry.controller.mappers;

import com.xmartin.carregistry.domain.Brand;
import com.xmartin.carregistry.controller.dtos.BrandRequest;
import com.xmartin.carregistry.controller.dtos.BrandResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrandMapper {

    public Brand toModel(BrandRequest brandRequest) {
        if (brandRequest == null) return null;
        Brand brand = new Brand();
        brand.setName(brandRequest.getName());
        brand.setCountry(brandRequest.getCountry());
        brand.setWarranty(brandRequest.getWarranty());
        return brand;
    }

    public BrandResponse toResponse(Brand brand) {
        if (brand == null) return null;
        BrandResponse brandResponse = new BrandResponse();
        brandResponse.setId(brand.getId());
        brandResponse.setName(brand.getName());
        brandResponse.setCountry(brand.getCountry());
        brandResponse.setWarranty(brand.getWarranty());
        return brandResponse;
    }

    public List<BrandResponse> toResponseList(List<Brand> brandList){
        return brandList.stream().map(this::toResponse).toList();
    }
}
