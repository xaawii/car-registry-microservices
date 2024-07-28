package com.xmartin.brand_service.repository;


import com.xmartin.brand_service.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<BrandEntity, Integer> {

    Optional<BrandEntity> findByNameIgnoreCase(String name);

}
