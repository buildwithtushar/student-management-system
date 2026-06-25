package com.platformcommons.sms.repository;

import com.platformcommons.sms.entity.CourseTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseTopicRepository extends JpaRepository<CourseTopic, Long> {
}