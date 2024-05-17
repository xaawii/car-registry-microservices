package com.xmartin.carregistry.controller;

import com.xmartin.carregistry.controller.dtos.BrandRequest;
import com.xmartin.carregistry.controller.dtos.BrandResponse;
import com.xmartin.carregistry.controller.mappers.BrandMapper;
import com.xmartin.carregistry.exceptions.BrandConflictException;
import com.xmartin.carregistry.exceptions.BrandNotFoundException;
import com.xmartin.carregistry.exceptions.FailedToLoadBrandsException;
import com.xmartin.carregistry.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequestMapping("/concessionaire/brands")
@RequiredArgsConstructor
public class BrandController {


    private final BrandService service;

    private final BrandMapper brandMapper;

    /*
    endpoint para añadir una sola marca. Si hay un error devuelve error 500, si no devuelve el objeto response
     */
    @Operation(summary = "Add brand", description = "Adds a brand into the brands table.")
    @PostMapping
    public ResponseEntity<?> addBrand(@RequestBody BrandRequest request) {
        try {
            BrandResponse response = brandMapper.toResponse(service.addBrand(brandMapper.toModel(request)));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BrandConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
    endpoint para eliminar una sola marca por ID. Si hay un error devuelve error 500 y si no lo encuentra
     devuelve error 404 noy found, si lo encuentra devuelve un mensaje con el id del objeto eliminado.
     */
    @Operation(summary = "Delete brand by id", description = "Delete a brand identified by the specified brand ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBrand(@PathVariable Integer id) {
        try {
            service.deleteBrand(id);
            return ResponseEntity.ok("Deleted brand with id " + id);
        } catch (BrandNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
      endpoint para actualizar una sola marca por ID. Si hay un error devuelve error 500 y si no lo encuentra
      devuelve error 404 noy found, si lo encuentra devuelve el objeto actualizado.
     */
    @Operation(summary = "Update brand by id", description = "Update and return the data for the specified brand ID.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBrand(@RequestBody BrandRequest brandRequest, @PathVariable Integer id) {
        try {
            BrandResponse response = brandMapper.toResponse(service.updateBrand(brandMapper.toModel(brandRequest), id));

            return ResponseEntity.ok(response);

        } catch (BrandNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
      endpoint para obtener una sola marca por ID. Si hay un error devuelve error 500 y si no lo encuentra
      devuelve error 404 noy found, si lo encuentra devuelve el objeto response.
    */
    @Operation(summary = "Get brand by id", description = "Returns a brand data for the specified brand ID.")
    @GetMapping("/{id}")
    public ResponseEntity<?> getBrandById(@PathVariable Integer id) {
        try {
            BrandResponse response = brandMapper.toResponse(service.getBrandById(id));

            return ResponseEntity.ok(response);

        } catch (BrandNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
      endpoint para obtener todas las marcas. Si hay un error devuelve error 500 y si no devuelve el objeto response.
      será asincrono por lo que uso completable future.
    */
    @Operation(summary = "Get all brands", description = "Returns a list of brand data.")
    @GetMapping
    public CompletableFuture<ResponseEntity<?>> getBrands() {

        return service.getBrands()
                .thenApplyAsync(brandMapper::toResponseList)
                .thenApplyAsync(brands -> {
                    if (brands.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                    } else {
                        return ResponseEntity.ok(brands);
                    }
                })
                .exceptionallyAsync(throwable -> ResponseEntity.internalServerError().build());

    }

    @Operation(summary = "Upload brands with CSV", description = "Upload brands using a CSV file.")
    @PostMapping("/uploadCsv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing file");
        }

        if (Objects.requireNonNull(file.getOriginalFilename()).contains(".csv")) {
            try {
                return ResponseEntity.status(HttpStatus.CREATED).body(brandMapper.toResponseList(service.uploadBrands(file)));
            } catch (BrandConflictException | FailedToLoadBrandsException | NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The file it's not a CSV");

    }

    @Operation(summary = "Download CSV with brands", description = "Download a CSV file with brands data.")
    @GetMapping("/downloadCsv")
    public ResponseEntity<?> downloadCSV() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "brands_exported.csv");

        try {
            byte[] csvBytes = service.downloadBrands().getBytes();

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

}
