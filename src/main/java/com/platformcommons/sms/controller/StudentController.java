package com.platformcommons.sms.controller;

import com.platformcommons.sms.dto.*;
import com.platformcommons.sms.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "APIs for managing students, courses and enrollments")
public class StudentController {

    private final StudentService studentService;

    @Operation( summary = "Register a new student",
                description = "Admin registers a new student with personal details and addresses"
    )
    @PostMapping("/register")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        log.info("REST request to register student received.");
        StudentResponse response = studentService.registerStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation( summary = "Get student profile by ID",
                description = "Fetch complete profile of a student including enrolled courses"
    )
    @GetMapping("/{id}/profile")
    public ResponseEntity<StudentResponse> getStudentProfile(@PathVariable Long id) {
        log.info("REST request to fetch student profile ID: {}", id);
        StudentResponse response = studentService.getStudentProfile(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    @Operation( summary = "Update student profile",
                description = "Student updates their own profile — email, mobile number, parents names and addresses"
    )
    @PutMapping("/{id}/update-profile")
    public ResponseEntity<StudentResponse> updateStudentProfile(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request) {
        log.info("REST request to update student profile ID: {}", id);
        StudentResponse response = studentService.updateStudentProfile(id, request);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @Operation( summary = "Search students by name",
                description = "Admin searches for students using partial, case-insensitive name match. Supports pagination"
    )
    @GetMapping("/search-by-name")
    public ResponseEntity<Page<StudentResponse>> searchStudentsByName(@RequestParam String name,
                                                                      @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                                      @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                                      @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                      @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        log.info("REST request to search students by name: '{}'", name);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<StudentResponse> studentByNameResponses = studentService.searchStudentsByName(name, PageRequest.of(pageNo - 1, pageSize, sort));
        return new ResponseEntity<>(studentByNameResponses,HttpStatus.OK);
    }

    @Operation( summary = "Get all students enrolled in a course",
                description = "Admin fetches a paginated list of all students assigned to a specific course"
    )
    @GetMapping("/enrolled-in-course/{courseId}")
    public ResponseEntity<Page<StudentResponse>> getStudentsByCourse(
                                                @PathVariable Long courseId,
                                                @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        log.info("REST request to fetch students for course ID: {}", courseId);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<StudentResponse> studentsByCourse = studentService.getStudentsByCourse(courseId, PageRequest.of(pageNo - 1, pageSize, sort));
        return new ResponseEntity<>(studentsByCourse,HttpStatus.OK);
    }

    @Operation( summary = "Create a new course",
                description = "Admin creates a new course with name, type, duration and list of topics"
    )
    @PostMapping("/courses/create")
    public ResponseEntity<String> createCourse(@Valid @RequestBody CourseRequest request) {
        log.info("REST request to create course received.");
        studentService.createCourse(request);
        return new ResponseEntity<>("Course setup successful!",HttpStatus.CREATED);
    }

    @Operation( summary = "Search courses by course name",
                description = "Student searches for available courses using partial, case-insensitive course name match. Supports pagination"
    )
    @GetMapping("/courses/search-by-name")
    public ResponseEntity<Page<CourseResponse>> searchCoursesByName(
                                                @RequestParam String courseName,
                                                @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        log.info("REST request to search courses by name: '{}'", courseName);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<CourseResponse> courseResponses = studentService.searchCoursesByName(courseName, PageRequest.of(pageNo - 1, pageSize, sort));
        return new ResponseEntity<>(courseResponses,HttpStatus.OK);
    }

    @Operation( summary = "Search courses by topic",
                description = "Student searches for courses that contain a specific topic. Supports pagination"
    )
    @GetMapping("/courses/search-by-topic")
    public ResponseEntity<Page<CourseResponse>> searchCoursesByTopic(
                                                @RequestParam String topicName,
                                                @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        log.info("REST request to search courses by topic: '{}'", topicName);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<CourseResponse> courseResponses = studentService.searchCoursesByTopic(topicName, PageRequest.of(pageNo - 1, pageSize, sort));
        return new ResponseEntity<>(courseResponses,HttpStatus.OK);
    }

    @Operation( summary = "Enroll a student into a course",
                description = "Admin assigns a student to a course. Duplicate enrollment is rejected with 409 CONFLICT"
    )
    @PostMapping("/enroll-student-in-course")
    public ResponseEntity<String> enrollStudent(@RequestBody EnrollmentRequest request) {
        log.info("REST request to enroll student received.");
        studentService.enrollStudentInCourse(request);
        return new ResponseEntity<>("Student enrolled successfully into the course!",HttpStatus.CREATED);
    }

    @Operation( summary = "Unenroll a student from a course",
                description = "Student leaves (unenrolls from) a course they are currently assigned to"
    )
    @DeleteMapping("/{studentId}/unenroll-from-course/{courseId}")
    public ResponseEntity<String> unenrollStudent(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        log.info("REST request to unenroll student ID {} from course ID {}", studentId, courseId);
        studentService.unenrollStudentFromCourse(studentId, courseId);
        return new ResponseEntity<>("Student successfully unenrolled from the course.",HttpStatus.OK);
    }

}
