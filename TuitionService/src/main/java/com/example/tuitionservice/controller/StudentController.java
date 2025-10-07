package com.example.tuitionservice.controller;

import com.example.tuitionservice.model.Student;
import com.example.tuitionservice.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // API: GET /students/{mssv}/tuition
    @GetMapping("/{mssv}/tuition")
    public ResponseEntity<Student> getTuitionByStudentId(@PathVariable String mssv) {
        return studentRepository.findById(mssv)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

