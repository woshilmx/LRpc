package com.commen.service;

import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class Student  implements Serializable {
    private String name;

    public Student(String name) {
        this.name = name;
    }
}
