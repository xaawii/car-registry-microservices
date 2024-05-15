package com.xmartin.carregistry.service.impl;

import com.xmartin.carregistry.domain.Brand;
import com.xmartin.carregistry.entity.BrandEntity;
import com.xmartin.carregistry.exceptions.BrandConflictException;
import com.xmartin.carregistry.exceptions.BrandNotFoundException;
import com.xmartin.carregistry.exceptions.FailedToLoadBrandsException;
import com.xmartin.carregistry.repository.BrandRepository;
import com.xmartin.carregistry.service.BrandService;
import com.xmartin.carregistry.service.converters.BrandConverter;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository repository;

    private final BrandConverter brandConverter;
    private static final String[] HEADERS = {"name", "warranty", "country"};

    /*
    método para añadir una marca, la convierte de modelo a entity y la guarda en la bbdd, devuelve el
    objeto convertido a modelo.
     */
    @Override
    @Transactional
    public Brand addBrand(Brand brand) throws BrandConflictException {
        Optional<BrandEntity> brandEntity = repository.findByNameIgnoreCase(brand.getName());
        if (brandEntity.isPresent()) {
            throw new BrandConflictException("Brand with name " + brand.getName() + " already exists.");
        }

        return brandConverter.toBrand(repository.save(brandConverter.toEntity(brand)));
    }

    /*
    método para eliminar una marca por el id, busca si existe en la bbdd, si existe lo elimina y devuelve true, si no
    existe, devuelve false.
     */
    @Override
    @Transactional
    public void deleteBrand(Integer id) throws BrandNotFoundException {
        BrandEntity brandEntity = repository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand with ID " + id + " was not found"));

        repository.delete(brandEntity);


    }

    /*
    método para actualizar una marca, la busca en la bbdd con el id, si existe la convierte de modelo a entity, le settea
    por si acaso el id de la URL y lo guarda en la bbdd, devuelve el objeto convertido a modelo.
    Si no existe devuelve nulo.
     */
    @Override
    @Transactional
    public Brand updateBrand(Brand brand, Integer id) throws BrandNotFoundException {

        repository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand with ID " + id + " was not found"));

        BrandEntity brandEntity = brandConverter.toEntity(brand);
        brandEntity.setId(id);
        return brandConverter.toBrand(repository.save(brandEntity));

    }

    /*
    Usamos Async en este método ya que podría ser una consulta enorme con miles o millones de registros.
    Devuelve una lista de marcas convertida de entity a modelo.
     */
    @Override
    @Async
    public CompletableFuture<List<Brand>> getBrands() {
        return CompletableFuture.completedFuture(brandConverter.toBrandList(repository.findAll()));
    }

    /*
    método para obtener una marca con el id, si existe, devovlemos el objeto convertido a modelo, si no
    devolvemos nulo.
     */
    @Override
    public Brand getBrandById(Integer id) throws BrandNotFoundException {
        return repository.findById(id).map(brandConverter::toBrand)
                .orElseThrow(() -> new BrandNotFoundException("Brand with ID " + id + " was not found"));
    }

    @Override
    public List<Brand> uploadBrands(MultipartFile file) throws BrandConflictException, FailedToLoadBrandsException {

        List<BrandEntity> brandEntityList = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {


            Iterable<CSVRecord> csvRecordList = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecordList) {
                if (repository.findByNameIgnoreCase(csvRecord.get("name")).isPresent()) {
                    throw new BrandConflictException("Brand already exists");
                }

                BrandEntity brandEntity = BrandEntity.builder()
                        .name(csvRecord.get("name"))
                        .warranty(Integer.valueOf(csvRecord.get("warranty")))
                        .country(csvRecord.get("country"))
                        .build();

                brandEntityList.add(brandEntity);
            }

            return brandConverter.toBrandList(repository.saveAll(brandEntityList));

        } catch (IOException e) {
            log.error("Failed to upload cars");
            throw new FailedToLoadBrandsException("Failed to load brands from cvs");
        }
    }

    @Override
    public String downloadBrands() {
        List<BrandEntity> brandEntityList = repository.findAll();
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(Arrays.toString(HEADERS).replace("[", "")
                .replace("]", "").replace(" ", "")).append("\n");

        brandEntityList.forEach(brandEntity -> csvContent
                .append(brandEntity.getName()).append(",")
                .append(brandEntity.getWarranty()).append(",")
                .append(brandEntity.getCountry()).append("\n"));

        return csvContent.toString();
    }
}
