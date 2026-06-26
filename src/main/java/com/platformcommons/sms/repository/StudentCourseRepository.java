package com.platformcommons.sms.repository;

import com.platformcommons.sms.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByStudentId(Long studentId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId);
}