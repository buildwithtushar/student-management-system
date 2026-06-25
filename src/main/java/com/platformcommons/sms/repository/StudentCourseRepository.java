package com.platformcommons.sms.repository;

import com.platformcommons.sms.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
}