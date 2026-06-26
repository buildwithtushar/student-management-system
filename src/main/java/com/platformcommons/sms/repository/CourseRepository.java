package com.platformcommons.sms.repository;

import com.platformcommons.sms.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByCourseNameContainingIgnoreCase(String courseName, Pageable pageable);
    @Query("""
            SELECT DISTINCT c FROM Course c
            JOIN c.topics t
            WHERE LOWER(t.topicName) LIKE LOWER(CONCAT('%', :topicName, '%'))
            """)
    Page<Course> findCoursesByTopicName(@Param("topicName") String topicName, Pageable pageable);
}