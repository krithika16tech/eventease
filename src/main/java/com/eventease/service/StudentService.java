package com.eventease.service;

import com.eventease.dto.StudentCreateRequest;
import com.eventease.dto.StudentResponse;
import com.eventease.entity.Student;
import com.eventease.exception.DuplicateRegistrationException;
import com.eventease.exception.StudentNotFoundException;
import com.eventease.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public StudentResponse createStudent(StudentCreateRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Student with email '" + request.getEmail() + "' already exists.");
        }
        if (studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new IllegalArgumentException("Student with roll number '" + request.getRollNumber() + "' already exists.");
        }

        Student student = new Student(request.getName(), request.getEmail(), request.getRollNumber());
        Student saved = studentRepository.save(student);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public StudentResponse mapToResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getRollNumber()
        );
    }
}
