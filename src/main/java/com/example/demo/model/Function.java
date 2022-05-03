package com.example.demo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Function {
    private String name;
    private String projectId;
}
