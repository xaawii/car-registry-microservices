package com.xmartin.brand_service.service;


import com.xmartin.brand_service.domain.Brand;
import com.xmartin.brand_service.exceptions.BrandConflictException;
import com.xmartin.brand_service.exceptions.BrandNotFoundException;
import com.xmartin.brand_service.exceptions.FailedToLoadBrandsException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface BrandService {
    Brand addBrand(Brand brand) throws BrandConflictException;

    void deleteBrand(Integer id) throws BrandNotFoundException;

    Brand updateBrand(Brand brand, Integer id) throws BrandNotFoundException;

    CompletableFuture<List<Brand>> getBrands();

    Brand getBrandById(Integer id) throws BrandNotFoundException;

    Brand getBrandByName(String name) throws BrandNotFoundException;

    List<Brand> uploadBrands(MultipartFile file) throws BrandConflictException, FailedToLoadBrandsException;

    String downloadBrands();
}
