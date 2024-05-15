package com.xmartin.carregistry.controller.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarListResponse {
    private int page;
    private int pageSize;
    private int elements;
    private List<CarResponse> carList;
}
