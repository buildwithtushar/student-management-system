package com.platformcommons.sms.repository;

import com.platformcommons.sms.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentCode(String studentCode);

    Page<Student> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
            SELECT s FROM Student s
            JOIN s.studentCourses sc
            WHERE sc.course.id = :courseId
            """)
    Page<Student> findStudentsByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @EntityGraph(attributePaths = {"studentCourses", "studentCourses.course"})
    Optional<Student> findById(Long studentId);
}