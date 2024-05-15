package com.xmartin.carregistry.repository;

import com.xmartin.carregistry.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<BrandEntity, Integer> {

    Optional<BrandEntity> findByNameIgnoreCase(String name);

}
