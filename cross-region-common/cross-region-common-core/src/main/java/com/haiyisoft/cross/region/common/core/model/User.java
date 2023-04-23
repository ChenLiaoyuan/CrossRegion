package com.haiyisoft.cross.region.common.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    private Long id;
    private String name;
    private Integer age;

}