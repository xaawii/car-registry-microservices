package com.xmartin.carregistry.controller;


import com.xmartin.carregistry.controller.dtos.CarListResponse;
import com.xmartin.carregistry.controller.dtos.CarRequest;
import com.xmartin.carregistry.controller.dtos.CarResponse;
import com.xmartin.carregistry.controller.mappers.CarMapper;
import com.xmartin.carregistry.exceptions.BrandNotFoundException;
import com.xmartin.carregistry.exceptions.CarNotFoundException;
import com.xmartin.carregistry.exceptions.FailedToLoadCarsException;
import com.xmartin.carregistry.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequestMapping("/concessionaire/cars")
@RequiredArgsConstructor
public class CarController {

    //proporcionamos instancia del service
    private final CarService service;

    //proporcionamos instancia del mapper de coche
    private final CarMapper carMapper;

    //endpoint para añadir un solo coche
    @Operation(summary = "Add car", description = "Adds a car into cars table.")
    @PostMapping
    public ResponseEntity<?> addCar(@RequestBody CarRequest carRequest) {
        try {

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(carMapper.toResponse(service.addCar(carMapper.toModel(carRequest))));

        } catch (BrandNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //endpoint para añadir una lista de coches, será asincrono por lo que uso CompletableFuture
    @Operation(summary = "Add cars", description = "Add a list of cars into cars table.")
    @PostMapping("/batch")
    public CompletableFuture<ResponseEntity<List<CarResponse>>> addCars(@RequestBody List<CarRequest> carRequest) {

        try {
            return service.addCars(carMapper.toModelList(carRequest))
                    .thenApplyAsync(carMapper::toResponseList)
                    .thenApplyAsync(ResponseEntity::ok)
                    .exceptionallyAsync(throwable -> ResponseEntity.internalServerError().build());
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }

    }

    //endpoint para consultar un solo coche con el ID, si no lo encuentra devuelve 404 not found.
    @Operation(summary = "Get car by id", description = "Returns a car data for the specified car ID.")
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Integer id) {

        try {
            CarResponse response = carMapper.toResponse(service.getCarById(id));

            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    //endpoint para consultar los coches, será asincrono por lo que uso completable future.
    @Operation(summary = "Get cars", description = "Returns a list of car data.")
    @GetMapping
    public CompletableFuture<ResponseEntity<?>> getCars(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {


        return service.getCars(PageRequest.of(page, size))
                .thenApplyAsync(carMapper::toResponseList)
                .thenApplyAsync(cars -> {
                    if (cars.isEmpty()) {
                        return ResponseEntity.noContent().build();
                    } else {
                        return ResponseEntity.ok(
                                CarListResponse.builder()
                                        .page(page)
                                        .pageSize(size)
                                        .elements(cars.size())
                                        .carList(cars)
                                        .build()
                        );
                    }
                })
                .exceptionallyAsync(throwable -> ResponseEntity.internalServerError().build());

    }

    /*
    endpoint para actualizar un solo coche con el ID y el objeto actualizado, si no lo encuentra en la bbdd
    devuelve 404 not found.
     */
    @Operation(summary = "Update car by id", description = "Update and return the data for the specified car ID.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCar(@RequestBody CarRequest carRequest, @PathVariable Integer id) {
        try {
            CarResponse response = carMapper.toResponse(service.updateCar(carMapper.toModel(carRequest), id));
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

        } catch (CarNotFoundException | BrandNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //endpoint para eliminar un solo coche con el ID, si no lo encuentra devuelve 404 not found.
    @Operation(summary = "Delete car by id", description = "Delete a car identified by the specified car ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCar(@PathVariable Integer id) {
        try {
            service.deleteCar(id);
            return ResponseEntity.ok("Car with id " + id + " deleted");

        } catch (CarNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Upload cars with CSV", description = "Upload cars using a CSV file.")
    @PostMapping("/uploadCsv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing file");
        }

        if (Objects.requireNonNull(file.getOriginalFilename()).contains(".csv")) {
            try {
                return ResponseEntity.status(HttpStatus.CREATED).body(carMapper.toResponseList(service.uploadCars(file)));
            } catch (BrandNotFoundException | FailedToLoadCarsException | NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The file it's not a CSV");

    }

    @Operation(summary = "Download CSV with cars", description = "Download a CSV file with cars data.")
    @GetMapping("/downloadCsv")
    public ResponseEntity<?> downloadCSV() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "cars_exported.csv");

        try {
            byte[] csvBytes = service.downloadCars().getBytes();

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
}
