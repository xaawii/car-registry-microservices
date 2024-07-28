package com.xmartin.carregistry.repository;

import com.xmartin.carregistry.entity.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<CarEntity, Integer> {

    void deleteAllByBrandId(Integer brandId);

}
