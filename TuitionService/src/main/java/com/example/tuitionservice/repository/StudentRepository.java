package com.example.tuitionservice.repository;

import com.example.tuitionservice.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String>{
}