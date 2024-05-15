package com.xmartin.carregistry.service;

import com.xmartin.carregistry.domain.Brand;
import com.xmartin.carregistry.exceptions.BrandConflictException;
import com.xmartin.carregistry.exceptions.BrandNotFoundException;
import com.xmartin.carregistry.exceptions.FailedToLoadBrandsException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BrandService {
    Brand addBrand(Brand brand) throws BrandConflictException;
    void deleteBrand(Integer id) throws BrandNotFoundException;
    Brand updateBrand(Brand brand, Integer id) throws BrandNotFoundException;
    CompletableFuture<List<Brand>> getBrands();
    Brand getBrandById(Integer id) throws BrandNotFoundException;

    List<Brand> uploadBrands(MultipartFile file) throws BrandConflictException, FailedToLoadBrandsException;

    String downloadBrands();
}
