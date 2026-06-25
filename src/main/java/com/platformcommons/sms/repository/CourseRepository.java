package com.platformcommons.sms.repository;

import com.platformcommons.sms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}