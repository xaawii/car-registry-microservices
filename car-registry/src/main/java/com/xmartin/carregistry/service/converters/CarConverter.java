package com.xmartin.carregistry.service.converters;

import com.xmartin.carregistry.domain.Car;
import com.xmartin.carregistry.entity.CarEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CarConverter {

    private final BrandConverter brandConverter;

    public Car toCar(CarEntity carEntity) {
        Car car = new Car();
        car.setId(carEntity.getId());
        car.setBrand(brandConverter.toBrand(carEntity.getBrand()));
        car.setColour(carEntity.getColour());
        car.setMileage(carEntity.getMileage());
        car.setDescription(carEntity.getDescription());
        car.setModel(carEntity.getModel());
        car.setFuelType(carEntity.getFuelType());
        car.setYear(carEntity.getYear());
        car.setPrice(carEntity.getPrice());
        car.setNumDoors(carEntity.getNumDoors());
        return car;
    }

    public CarEntity toEntity(Car car) {
        CarEntity carEntity = new CarEntity();
        carEntity.setId(car.getId());
        carEntity.setBrand(brandConverter.toEntity(car.getBrand()));
        carEntity.setColour(car.getColour());
        carEntity.setMileage(car.getMileage());
        carEntity.setDescription(car.getDescription());
        carEntity.setModel(car.getModel());
        carEntity.setFuelType(car.getFuelType());
        carEntity.setYear(car.getYear());
        carEntity.setPrice(car.getPrice());
        carEntity.setNumDoors(car.getNumDoors());
        return carEntity;
    }

    public List<Car> toCarList(List<CarEntity> carEntities) {
        return carEntities.stream().map(this::toCar).toList();
    }

    public List<CarEntity> toEntityList(List<Car> cars) {
        return cars.stream().map(this::toEntity).toList();
    }
}
