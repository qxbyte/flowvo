package org.xue.core.functioncall.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "business")
@Data
public class Business extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    private String status;

    // Getter/Setter
}

