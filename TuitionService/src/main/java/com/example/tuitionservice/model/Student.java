package com.example.tuitionservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    @Column(name = "mssv")
    private String studentId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "tuitionFee", nullable = false)
    private BigDecimal tuitionFee;

    @Column(name = "status", nullable = false)
    private String status;
}
