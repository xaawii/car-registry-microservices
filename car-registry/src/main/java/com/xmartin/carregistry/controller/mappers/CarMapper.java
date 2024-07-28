package com.xmartin.carregistry.controller.mappers;

import com.xmartin.carregistry.controller.dtos.CarRequest;
import com.xmartin.carregistry.controller.dtos.CarResponse;
import com.xmartin.carregistry.domain.Brand;
import com.xmartin.carregistry.domain.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CarMapper {
    private final BrandMapper brandMapper;

    public Car toModel(CarRequest carRequest) {
        if (carRequest == null) return null;
        Car car = new Car();
        Brand brand = new Brand();
        brand.setName(carRequest.getBrand());
        car.setBrand(brand);
        car.setColour(carRequest.getColour());
        car.setMileage(carRequest.getMileage());
        car.setDescription(carRequest.getDescription());
        car.setFuelType(carRequest.getFuelType());
        car.setModel(carRequest.getModel());
        car.setYear(carRequest.getYear());
        car.setPrice(carRequest.getPrice());
        car.setNumDoors(carRequest.getNumDoors());
        return car;
    }

    public CarResponse toResponse(Car car) {
        if (car == null) return null;
        CarResponse carResponse = new CarResponse();
        carResponse.setId(car.getId());
        carResponse.setBrand(brandMapper.toResponse(car.getBrand()));
        carResponse.setColour(car.getColour());
        carResponse.setMileage(car.getMileage());
        carResponse.setDescription(car.getDescription());
        carResponse.setFuelType(car.getFuelType());
        carResponse.setModel(car.getModel());
        carResponse.setYear(car.getYear());
        carResponse.setPrice(car.getPrice());
        carResponse.setNumDoors(car.getNumDoors());
        return carResponse;
    }

    public List<CarResponse> toResponseList(List<Car> carList) {
        return carList.stream().map(this::toResponse).toList();
    }

    public List<Car> toModelList(List<CarRequest> carRequestList) {
        return carRequestList.stream().map(this::toModel).toList();
    }

}
