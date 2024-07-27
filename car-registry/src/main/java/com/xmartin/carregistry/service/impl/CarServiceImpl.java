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
import java.util.*;
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
    private static final String[] HEADERS = {"brand_id", "model", "description", "colour", "fuel_type"
            , "mileage", "num_doors", "price", "year"};

    /*
     método para añadir un coche, lo convierte de modelo a entity y lo guarda en la bbdd, devuelve el
     objeto convertido a modelo.
     */
    @Override
    @Transactional
    public Car addCar(Car car) throws BrandNotFoundException {

        Brand brand = brandClient.getBrandById(car.getBrand().getId())
                .orElseThrow(() -> new BrandNotFoundException("Brand with ID "
                        + car.getBrand().getId() + " was not found"));

        CarEntity newCar = carConverter.toEntity(car);
        newCar.setBrandId(brand.getId());
        return carConverter.toCar(carRepository.save(newCar));


    }

    /*
     Usamos Async en este método ya que podría ser una consulta enorme con miles o millones de registros.
     Devuelve una lista de coches convertida de entity a modelo.
      */
    @Override
    @Async
    @Transactional
    public CompletableFuture<List<Car>> getCars(Pageable pageable) {
        List<Car> carList = carConverter
                .toCarList(carRepository.findAll(pageable).stream().toList());

        Map<Integer, Brand> brandMap = brandClient.getAllBrands().stream()
                .collect(Collectors.toMap(Brand::getId, Function.identity()));

        carList.forEach(car -> car.setBrand(brandMap.get(car.getBrand().getId())));

        return CompletableFuture.completedFuture(carList);
    }

    /*
    método para obtener un coche con el id, si existe, devovlemos el objeto convertido a modelo, si no
    devolvemos nulo.
     */
    @Override
    @Transactional
    public Car getCarById(Integer id) throws BrandNotFoundException {

        Car car = carRepository.findById(id).map(carConverter::toCar).orElse(null);
        if (car != null) {
            Brand brand = brandClient.getBrandById(car.getBrand().getId())
                    .orElseThrow(() -> new BrandNotFoundException("Brand with ID "
                            + car.getBrand().getId() + " was not found"));

            car.setBrand(brand);
            return car;
        }
        return null;
    }

    /*
    método para actualizar un coche, lo busca en la bbdd con el id, si existe lo convierte de modelo a entity, le settea
    por si acaso el id de la URL y lo guarda en la bbdd, devuelve el objeto convertido a modelo.
    Si no existe devuelve nulo.
     */
    @Override
    @Transactional
    public Car updateCar(Car car, Integer id) throws CarNotFoundException, BrandNotFoundException {

        carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car with ID " + id + " was not found"));

        Brand brand = brandClient.getBrandById(car.getBrand().getId())
                .orElseThrow(() -> new BrandNotFoundException("Brand with ID "
                        + car.getBrand().getId() + " was not found"));

        CarEntity carEntity = carConverter.toEntity(car);
        carEntity.setId(id);
        carEntity.setBrandId(brand.getId());
        return carConverter.toCar(carRepository.save(carEntity));


    }

    /*
    método para eliminar un coche por el id, busca si existe en la bbdd, si existe lo elimina y devuelve true, si no
    existe, devuelve false.
     */
    @Override
    @Transactional
    public void deleteCar(Integer id) throws CarNotFoundException {
        CarEntity carEntity = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car with ID " + id + " was not found"));

        brandClient.deleteBrandById(carEntity.getBrandId());

        carRepository.delete(carEntity);

    }

    /*
    Usamos Async en este método ya que podríamos insertar una enorme cantidad de registros y las inserciones
    pueden ser lentas.
    Convierte una lista de coches modelo a entidad y por cada uno comprueba que tengan como marca una que
    exista en la tabla Brand, si alguna no existe se lanza una excepción. Si tod está bien, se insertan en la
    bbdd y devuelve un completable Future con la lista de coches añadidos.
     */

    @Override
    @Async
    @Transactional
    public CompletableFuture<List<Car>> addCars(List<Car> cars) throws BrandNotFoundException {

        Map<Integer, Brand> brandMap = brandClient.getAllBrands().stream()
                .collect(Collectors.toMap(Brand::getId, Function.identity()));

        for (Car car : cars) {
            Brand brand = Optional.ofNullable(brandMap.get(car.getBrand().getId()))
                    .orElseThrow(() -> new BrandNotFoundException("Some brand not found"));
            car.setBrand(brand);
        }

        cars.forEach(car -> car.setBrand(brandMap.get(car.getBrand().getId())));


        List<CarEntity> carEntities = carConverter.toEntityList(cars);


        List<Car> savedCars = carRepository.saveAll(carEntities).stream().map(carConverter::toCar).toList();

        savedCars.forEach(car -> car.setBrand(brandMap.get(car.getBrand().getId())));

        return CompletableFuture.completedFuture(carConverter.toCarList(carEntities));

    }

    @Override
    public List<Car> uploadCars(MultipartFile file) throws BrandNotFoundException, FailedToLoadCarsException {

        List<CarEntity> carEntityList = new ArrayList<>();

        try (BufferedReader fileReader =
                     new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {


            Iterable<CSVRecord> csvRecordList = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecordList) {
                Brand brand = brandClient.getBrandById(Integer.valueOf(csvRecord.get("brand_id")))
                        .orElseThrow(() -> new BrandNotFoundException("Some brand was not found"));

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
            log.error("Failed to upload cars");
            throw new FailedToLoadCarsException("Failed to upload cars");
        }
    }

    @Override
    public String downloadCars() throws BrandNotFoundException {
        List<CarEntity> carEntityList = carRepository.findAll();
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(Arrays.toString(HEADERS).replace("[", "")
                .replace("]", "").replace(" ", "")).append("\n");

        for (CarEntity carEntity : carEntityList) {
            Brand brand = brandClient.getBrandById(carEntity.getBrandId())
                    .orElseThrow(() -> new BrandNotFoundException("Brand with ID "
                            + carEntity.getBrandId() + " was not found"));

            csvContent.append(brand.getId()).append(",")
                    .append(carEntity.getModel()).append(",")
                    .append(carEntity.getDescription()).append(",")
                    .append(carEntity.getColour()).append(",")
                    .append(carEntity.getFuelType()).append(",")
                    .append(carEntity.getMileage()).append(",")
                    .append(carEntity.getNumDoors()).append(",")
                    .append(carEntity.getPrice()).append(",")
                    .append(carEntity.getYear()).append("\n");
        }


        return csvContent.toString();
    }
}
