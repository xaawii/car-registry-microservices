package com.xmartin.carregistry.controller.mappers;

import com.xmartin.carregistry.controller.dtos.BrandResponse;
import com.xmartin.carregistry.domain.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {


    public BrandResponse toResponse(Brand brand) {
        if (brand == null) return null;
        BrandResponse brandResponse = new BrandResponse();
        brandResponse.setId(brand.getId());
        brandResponse.setName(brand.getName());
        brandResponse.setCountry(brand.getCountry());
        brandResponse.setWarranty(brand.getWarranty());
        return brandResponse;
    }

}
