package com.xmartin.carregistry.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand {
    private Integer id;
    private String name;
    private Integer warranty;
    private String country;
}
