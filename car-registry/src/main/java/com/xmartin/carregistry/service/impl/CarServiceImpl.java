package com.xmartin.carregistry.service.impl;


import com.xmartin.carregistry.domain.Car;
import com.xmartin.carregistry.entity.BrandEntity;
import com.xmartin.carregistry.entity.CarEntity;
import com.xmartin.carregistry.exceptions.BrandNotFoundException;
import com.xmartin.carregistry.exceptions.CarNotFoundException;
import com.xmartin.carregistry.exceptions.FailedToLoadCarsException;
import com.xmartin.carregistry.repository.BrandRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {


    private final CarRepository carRepository;

    private final BrandRepository brandRepository;

    private final CarConverter carConverter;
    private static final String[] HEADERS = {"brand", "model", "description", "colour", "fuel_type"
            , "mileage", "num_doors", "price", "year"};

    /*
     método para añadir un coche, lo convierte de modelo a entity y lo guarda en la bbdd, devuelve el
     objeto convertido a modelo.
     */
    @Override
    @Transactional
    public Car addCar(Car car) throws BrandNotFoundException {

        BrandEntity brandEntity = brandRepository.findByNameIgnoreCase(car.getBrand().getName())
                .orElseThrow(() -> new BrandNotFoundException("Brand with name " + car.getBrand().getName() + " was not found"));

        CarEntity newCar = carConverter.toEntity(car);
        newCar.setBrand(brandEntity);
        return carConverter.toCar(carRepository.save(newCar));


    }

    /*
     Usamos Async en este método ya que podría ser una consulta enorme con miles o millones de registros.
     Devuelve una lista de coches convertida de entity a modelo.
      */
    @Override
    @Async
    public CompletableFuture<List<Car>> getCars(Pageable pageable) {
        return CompletableFuture.completedFuture(carConverter
                .toCarList(carRepository.findAll(pageable).stream().toList()));
    }

    /*
    método para obtener un coche con el id, si existe, devovlemos el objeto convertido a modelo, si no
    devolvemos nulo.
     */
    @Override
    public Car getCarById(Integer id) {
        return carRepository.findById(id).map(carConverter::toCar).orElse(null);
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

        BrandEntity brandEntity = brandRepository.findByNameIgnoreCase(car.getBrand().getName())
                .orElseThrow(() -> new BrandNotFoundException("Brand with name " + car.getBrand().getName() + " was not found"));

        CarEntity carEntity = carConverter.toEntity(car);
        carEntity.setId(id);
        carEntity.setBrand(brandEntity);
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
    public CompletableFuture<List<Car>> addCars(List<Car> cars) throws BrandNotFoundException {

        List<CarEntity> carEntities = carConverter.toEntityList(cars);


        for (CarEntity carEntity : carEntities) {
            Optional<BrandEntity> brand = brandRepository.findByNameIgnoreCase(carEntity.getBrand().getName());
            if (brand.isPresent()) {
                carEntity.setBrand(brand.get());
            } else {
                throw new BrandNotFoundException("Some brand does not exist");
            }
        }

        carRepository.saveAll(carEntities);
        return CompletableFuture.completedFuture(carConverter.toCarList(carEntities));

    }

    @Override
    public List<Car> uploadCars(MultipartFile file) throws BrandNotFoundException, FailedToLoadCarsException {

        List<CarEntity> carEntityList = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {


            Iterable<CSVRecord> csvRecordList = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecordList) {
                BrandEntity brandEntity = brandRepository.findByNameIgnoreCase(csvRecord.get("brand"))
                        .orElseThrow(() -> new BrandNotFoundException("Some brand does not exist"));
                CarEntity carEntity = CarEntity.builder()
                        .brand(brandEntity)
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
    public String downloadCars() {
        List<CarEntity> carEntityList = carRepository.findAll();
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(Arrays.toString(HEADERS).replace("[", "")
                .replace("]", "").replace(" ", "")).append("\n");

        carEntityList.forEach(carEntity -> csvContent.append(carEntity.getBrand().getName()).append(",")
                .append(carEntity.getModel()).append(",")
                .append(carEntity.getDescription()).append(",")
                .append(carEntity.getColour()).append(",")
                .append(carEntity.getFuelType()).append(",")
                .append(carEntity.getMileage()).append(",")
                .append(carEntity.getNumDoors()).append(",")
                .append(carEntity.getPrice()).append(",")
                .append(carEntity.getYear()).append("\n"));

        return csvContent.toString();
    }
}
