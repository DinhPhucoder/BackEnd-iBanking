package com.example.tuitionservice.repository;

import com.example.tuitionservice.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String>{
    Optional<Student> findByStudentId(String mssv);
}