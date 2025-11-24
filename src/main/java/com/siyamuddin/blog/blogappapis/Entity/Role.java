package com.siyamuddin.blog.blogappapis.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Role {
    @Id
    private int id;
    private String name;
}
