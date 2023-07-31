package com.commen.service;

import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class Student  implements Serializable {
    private String name;

    public Student(String name) {
        this.name = name;
    }
}
