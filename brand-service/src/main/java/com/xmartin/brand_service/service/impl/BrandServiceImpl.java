package com.xmartin.brand_service.service.impl;


import com.xmartin.brand_service.client.CarClient;
import com.xmartin.brand_service.domain.Brand;
import com.xmartin.brand_service.entity.BrandEntity;
import com.xmartin.brand_service.exceptions.BrandConflictException;
import com.xmartin.brand_service.exceptions.BrandNotFoundException;
import com.xmartin.brand_service.exceptions.FailedToLoadBrandsException;
import com.xmartin.brand_service.repository.BrandRepository;
import com.xmartin.brand_service.service.BrandService;
import com.xmartin.brand_service.service.converters.BrandConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository repository;
    private final BrandConverter brandConverter;
    private final CarClient carClient;
    private static final String[] HEADERS = {"name", "warranty", "country"};

    @Override
    @Transactional
    public Brand addBrand(Brand brand) throws BrandConflictException {
        ensureBrandDoesNotExist(brand.getName());
        return saveBrand(brand);
    }

    @Override
    @Transactional
    public void deleteBrand(Integer id) throws BrandNotFoundException {
        BrandEntity brandEntity = findBrandEntityById(id);
        carClient.deleteAllCarsByBrandId(brandEntity.getId());
        repository.delete(brandEntity);
    }

    @Override
    @Transactional
    public Brand updateBrand(Brand brand, Integer id) throws BrandNotFoundException {
        findBrandEntityById(id);
        return saveBrand(brand, id);
    }

    @Override
    @Async
    public CompletableFuture<List<Brand>> getBrands() {
        return CompletableFuture.completedFuture(brandConverter.toBrandList(repository.findAll()));
    }

    @Override
    public Brand getBrandById(Integer id) throws BrandNotFoundException {
        return findBrandById(id);
    }

    @Override
    public Brand getBrandByName(String name) throws BrandNotFoundException {
        return findBrandByName(name);
    }

    @Override
    public List<Brand> uploadBrands(MultipartFile file) throws BrandConflictException, FailedToLoadBrandsException {
        List<BrandEntity> brandEntityList = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser.getRecords()) {
                ensureBrandDoesNotExist(csvRecord.get("name"));

                BrandEntity brandEntity = BrandEntity.builder()
                        .name(csvRecord.get("name"))
                        .warranty(Integer.valueOf(csvRecord.get("warranty")))
                        .country(csvRecord.get("country"))
                        .build();

                brandEntityList.add(brandEntity);
            }

            return brandConverter.toBrandList(repository.saveAll(brandEntityList));
        } catch (IOException e) {
            log.error("Failed to upload brands", e);
            throw new FailedToLoadBrandsException("Failed to load brands from CSV");
        }
    }

    @Override
    public String downloadBrands() {
        List<BrandEntity> brandEntityList = repository.findAll();
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(String.join(",", HEADERS)).append("\n");

        brandEntityList.forEach(brandEntity -> csvContent.append(String.format("%s,%d,%s%n",
                brandEntity.getName(), brandEntity.getWarranty(), brandEntity.getCountry())));

        return csvContent.toString();
    }

    // Métodos auxiliares privados

    private void ensureBrandDoesNotExist(String name) throws BrandConflictException {
        if (repository.findByNameIgnoreCase(name).isPresent()) {
            throw new BrandConflictException("Brand with name " + name + " already exists.");
        }
    }

    private BrandEntity findBrandEntityById(Integer id) throws BrandNotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand with ID " + id + " was not found"));
    }

    private Brand findBrandById(Integer id) throws BrandNotFoundException {
        return repository.findById(id).map(brandConverter::toBrand)
                .orElseThrow(() -> new BrandNotFoundException("Brand with ID " + id + " was not found"));
    }

    private Brand findBrandByName(String name) throws BrandNotFoundException {
        return repository.findByNameIgnoreCase(name).map(brandConverter::toBrand)
                .orElseThrow(() -> new BrandNotFoundException("Brand with name: " + name + " was not found"));
    }

    private Brand saveBrand(Brand brand) {
        BrandEntity brandEntity = brandConverter.toEntity(brand);
        return brandConverter.toBrand(repository.save(brandEntity));
    }

    private Brand saveBrand(Brand brand, Integer id) {
        BrandEntity brandEntity = brandConverter.toEntity(brand);
        brandEntity.setId(id);
        return brandConverter.toBrand(repository.save(brandEntity));
    }
}
