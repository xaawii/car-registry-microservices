package com.xmartin.carregistry.service.impl;


import com.xmartin.carregistry.client.BrandClient;
import com.xmartin.carregistry.domain.Brand;
import com.xmartin.carregistry.domain.Car;
import com.xmartin.carregistry.entity.CarEntity;
import com.xmartin.carregistry.exceptions.BrandNotFoundException;
import com.xmartin.carregistry.exceptions.CarNotFoundException;
import com.xmartin.carregistry.exceptions.FailedToLoadCarsException;
import com.xmartin.carregistry.repository.CarRepository;
import com.xmartin.carregistry.service.CarService;
import com.xmartin.carregistry.service.converters.CarConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final BrandClient brandClient;
    private final CarConverter carConverter;

    private static final String[] HEADERS = {"brand", "model", "description", "colour", "fuel_type",
            "mileage", "num_doors", "price", "year"};

    @Override
    @Transactional
    public Car addCar(Car car) throws BrandNotFoundException {
        Brand brand = getBrandByName(car.getBrand().getName());
        CarEntity newCar = carConverter.toEntity(car);
        newCar.setBrandId(brand.getId());

        Car savedCar = carConverter.toCar(carRepository.save(newCar));
        savedCar.setBrand(brand);
        return savedCar;
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<List<Car>> getCars(Pageable pageable) throws BrandNotFoundException {
        List<Brand> brandList = getAllBrands();
        List<Car> carList = carConverter.toCarList(carRepository.findAll(pageable).stream().toList());

        Map<Integer, Brand> brandMap = convertToBrandMap(Brand::getId, brandList);

        carList.forEach(car -> car.setBrand(brandMap.get(car.getBrand().getId())));
        return CompletableFuture.completedFuture(carList);
    }

    @Override
    @Transactional
    public Car getCarById(Integer id) throws BrandNotFoundException, CarNotFoundException {
        Car car = findCarById(id);
        Brand brand = getBrandById(car.getBrand().getId());
        car.setBrand(brand);
        return car;
    }

    @Override
    @Transactional
    public Car updateCar(Car car, Integer id) throws CarNotFoundException, BrandNotFoundException {
        ensureCarExists(id);

        Brand brand = getBrandByName(car.getBrand().getName());
        CarEntity updatedCarEntity = carConverter.toEntity(car);
        updatedCarEntity.setId(id);
        updatedCarEntity.setBrandId(brand.getId());

        Car updatedCar = carConverter.toCar(carRepository.save(updatedCarEntity));
        updatedCar.setBrand(brand);
        return updatedCar;
    }

    @Override
    @Transactional
    public void deleteCar(Integer id) throws CarNotFoundException {
        CarEntity carEntity = findCarEntityById(id);
        carRepository.delete(carEntity);
    }

    @Override
    @Transactional
    public void deleteAllCarsByBrandId(Integer brandId) {
        carRepository.deleteAllByBrandId(brandId);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<List<Car>> addCars(List<Car> cars) throws BrandNotFoundException {
        Map<String, Brand> brandMap = getBrandMap(Brand::getName);

        for (Car car : cars) {
            Brand brand = brandMap.get(car.getBrand().getName());
            if (brand == null) {
                throw new BrandNotFoundException("Some brand not found");
            }
            car.setBrand(brand);
        }

        List<CarEntity> carEntities = carConverter.toEntityList(cars);
        List<Car> savedCars = carRepository.saveAll(carEntities).stream().map(carConverter::toCar).toList();

        Map<Integer, Brand> brandIdMap = getBrandMap(Brand::getId);
        savedCars.forEach(car -> car.setBrand(brandIdMap.get(car.getBrand().getId())));

        return CompletableFuture.completedFuture(savedCars);
    }

    @Override
    public List<Car> uploadCars(MultipartFile file) throws BrandNotFoundException, FailedToLoadCarsException {
        List<CarEntity> carEntityList = new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser.getRecords()) {
                Brand brand = getBrandByName(csvRecord.get("brand"));
                CarEntity carEntity = CarEntity.builder()
                        .brandId(brand.getId())
                        .model(csvRecord.get("model"))
                        .description(csvRecord.get("description"))
                        .colour(csvRecord.get("colour"))
                        .fuelType(csvRecord.get("fuel_type"))
                        .mileage(Integer.valueOf(csvRecord.get("mileage")))
                        .numDoors(Integer.valueOf(csvRecord.get("num_doors")))
                        .price(Double.valueOf(csvRecord.get("price")))
                        .year(Integer.valueOf(csvRecord.get("year")))
                        .build();

                carEntityList.add(carEntity);
            }
            return carConverter.toCarList(carRepository.saveAll(carEntityList));

        } catch (IOException e) {
            log.error("Failed to upload cars", e);
            throw new FailedToLoadCarsException("Failed to upload cars");
        }
    }

    @Override
    public String downloadCars() throws BrandNotFoundException {
        List<CarEntity> carEntityList = carRepository.findAll();
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(String.join(",", HEADERS)).append("\n");

        for (CarEntity carEntity : carEntityList) {
            Brand brand = getBrandById(carEntity.getBrandId());
            csvContent.append(String.format("%s,%s,%s,%s,%s,%d,%d,%f,%d%n",
                    brand.getName(),
                    carEntity.getModel(),
                    carEntity.getDescription(),
                    carEntity.getColour(),
                    carEntity.getFuelType(),
                    carEntity.getMileage(),
                    carEntity.getNumDoors(),
                    carEntity.getPrice(),
                    carEntity.getYear()));
        }
        return csvContent.toString();
    }

    // Métodos auxiliares privados

    private Car findCarById(Integer id) throws CarNotFoundException {
        return carRepository.findById(id).map(carConverter::toCar)
                .orElseThrow(() -> new CarNotFoundException("Car with ID " + id + " was not found"));
    }

    private CarEntity findCarEntityById(Integer id) throws CarNotFoundException {
        return carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car with ID " + id + " was not found"));
    }

    private void ensureCarExists(Integer id) throws CarNotFoundException {
        if (!carRepository.existsById(id)) {
            throw new CarNotFoundException("Car with ID " + id + " was not found");
        }
    }

    private Brand getBrandByName(String name) throws BrandNotFoundException {
        return brandClient.getBrandByName(name)
                .orElseThrow(() -> new BrandNotFoundException("Brand with name: " + name + " was not found"));
    }

    private Brand getBrandById(Integer id) throws BrandNotFoundException {
        return brandClient.getBrandById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand with ID " + id + " was not found"));
    }

    private List<Brand> getAllBrands() throws BrandNotFoundException {
        return Optional.ofNullable(brandClient.getAllBrands())
                .orElseThrow(() -> new BrandNotFoundException("No brands found"));
    }

    private <K> Map<K, Brand> getBrandMap(Function<Brand, K> keyMapper) throws BrandNotFoundException {
        List<Brand> brandList = getAllBrands();
        return convertToBrandMap(keyMapper, brandList);
    }

    private <K> Map<K, Brand> convertToBrandMap(Function<Brand, K> keyMapper, List<Brand> brandList) {
        return brandList.stream().collect(Collectors.toMap(keyMapper, Function.identity()));
    }
}