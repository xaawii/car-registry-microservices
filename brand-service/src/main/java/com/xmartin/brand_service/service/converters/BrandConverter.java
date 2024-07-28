package com.xmartin.brand_service.service.converters;


import com.xmartin.brand_service.domain.Brand;
import com.xmartin.brand_service.entity.BrandEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrandConverter {


    public Brand toBrand(BrandEntity brandEntity) {
        Brand brand = new Brand();
        brand.setId(brandEntity.getId());
        brand.setName(brandEntity.getName());
        brand.setCountry(brandEntity.getCountry());
        brand.setWarranty(brandEntity.getWarranty());
        return brand;
    }

    public BrandEntity toEntity(Brand brand) {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setId(brand.getId());
        brandEntity.setName(brand.getName());
        brandEntity.setCountry(brand.getCountry());
        brandEntity.setWarranty(brand.getWarranty());
        return brandEntity;
    }

    public List<Brand> toBrandList(List<BrandEntity> brandEntities) {
        return brandEntities.stream().map(this::toBrand).toList();
    }

    public List<BrandEntity> toEntityList(List<Brand> brands) {
        return brands.stream().map(this::toEntity).toList();
    }
}
