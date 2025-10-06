package com.example.tuitionservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.*;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    @Column(name = "mssv")
    private String mssv;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "tuition_fee", nullable = false)
    private Double tuitionFee;

    @Column(name = "status", nullable = false)
    private String status;
}
