package com.platformcommons.sms.service;

import com.platformcommons.sms.dto.*;
import com.platformcommons.sms.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {
    StudentResponse registerStudent(StudentRequest request);
    void createCourse(CourseRequest request);
    void enrollStudentInCourse(EnrollmentRequest request);
    StudentResponse getStudentProfile(Long studentId);

    StudentResponse updateStudentProfile(Long studentId, StudentUpdateRequest request);
    void unenrollStudentFromCourse(Long studentId, Long courseId);

    Page<StudentResponse> searchStudentsByName(String name, Pageable pageable);
    Page<StudentResponse> getStudentsByCourse(Long courseId, Pageable pageable);

    Page<CourseResponse> searchCoursesByName(String courseName, Pageable pageable);
    Page<CourseResponse> searchCoursesByTopic(String topicName, Pageable pageable);

}
