package com.xmartin.carregistry.service;

import com.xmartin.carregistry.domain.Car;
import com.xmartin.carregistry.exceptions.BrandNotFoundException;
import com.xmartin.carregistry.exceptions.CarNotFoundException;
import com.xmartin.carregistry.exceptions.FailedToLoadCarsException;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CarService {
    Car addCar(Car car) throws BrandNotFoundException;

    CompletableFuture<List<Car>> getCars(Pageable pageable);

    Car getCarById(Integer id) throws BrandNotFoundException;

    Car updateCar(Car car, Integer id) throws CarNotFoundException, BrandNotFoundException;

    void deleteCar(Integer id) throws CarNotFoundException;

    CompletableFuture<List<Car>> addCars(List<Car> cars) throws BrandNotFoundException;

    List<Car> uploadCars(MultipartFile file) throws BrandNotFoundException, FailedToLoadCarsException;

    String downloadCars() throws BrandNotFoundException;
}
