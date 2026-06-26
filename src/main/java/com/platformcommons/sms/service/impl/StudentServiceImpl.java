package com.platformcommons.sms.service.impl;

import com.platformcommons.sms.dto.*;
import com.platformcommons.sms.entity.*;
import com.platformcommons.sms.exception.DuplicateResourceException;
import com.platformcommons.sms.exception.ResourceNotFoundException;
import com.platformcommons.sms.repository.CourseRepository;
import com.platformcommons.sms.repository.StudentCourseRepository;
import com.platformcommons.sms.repository.StudentRepository;
import com.platformcommons.sms.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public StudentResponse registerStudent(StudentRequest request) {
        log.info("Processing student admission request. StudentCode=[{}]", request.getStudentCode());

        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            log.error("Registration failed. Student code {} already exists", request.getStudentCode());
            throw new DuplicateResourceException(
                    "Student code '" + request.getStudentCode() + "' is already registered in the system");
        }

        Student student = modelMapper.map(request, Student.class);

        if (student.getAddresses() != null) {
            log.debug("Mapping back-references for [{}] explicit addresses on StudentCode=[{}]");
            student.getAddresses().forEach(address -> address.setStudent(student));
        }

        Student savedStudent = studentRepository.save(student);
        log.info("Student successfully registered with database ID: {}", savedStudent.getId());

        return buildStudentResponse(savedStudent);
    }

    @Override
    @Transactional
    public void createCourse(CourseRequest request) {
        log.info("Creating a new course: {}", request.getCourseName());

        Course course = modelMapper.map(request, Course.class);

        if (request.getTopics() != null) {
            log.debug("Compiling [{}] core syllabus structural modules for course allocation context", request.getTopics().size());
            List<CourseTopic> topics = request.getTopics().stream()
                    .map(topicName -> {
                        CourseTopic topic = new CourseTopic();
                        topic.setTopicName(topicName);
                        topic.setCourse(course);
                        return topic;
                    }).collect(Collectors.toList());
            course.setTopics(topics);
        }

        courseRepository.save(course);
        log.info("Course '{}' successfully saved.", request.getCourseName());
    }

    @Override
    @Transactional
    public void enrollStudentInCourse(EnrollmentRequest request) {
        log.info("Enrolling student ID {} into course ID {}", request.getStudentId(), request.getCourseId());

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> {
                    log.error("Enrollment failed. Student ID {} not found", request.getStudentId());
                    return new ResourceNotFoundException("Student not found with ID: " + request.getStudentId());
                });

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {
                    log.error("Enrollment failed. Course ID {} not found", request.getCourseId());
                    return new ResourceNotFoundException("Course not found with ID: " + request.getCourseId());
                });

        if (studentCourseRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            log.warn("Student ID {} is already enrolled in course ID {}", student.getId(), course.getId());
            throw new DuplicateResourceException(
                    "Student ID " + student.getId() + " is already enrolled in course ID " + course.getId());
        }

        StudentCourse enrollment = new StudentCourse();
        enrollment.setStudent(student);
        enrollment.setCourse(course);

        studentCourseRepository.save(enrollment);
        log.info("Student ID {} successfully enrolled in course ID {}.", student.getId(), course.getId());
    }


    @Override
    @Transactional
    public void unenrollStudentFromCourse(Long studentId, Long courseId) {
        log.info("Unenrolling student ID {} from course ID {}", studentId, courseId);

        StudentCourse enrollment = studentCourseRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> {
                    log.error("Unenrollment failed. No enrollment found for student ID {} and course ID {}",
                            studentId, courseId);
                    return new ResourceNotFoundException(
                            "Enrollment not found for student ID " + studentId + " and course ID " + courseId);
                });

        studentCourseRepository.delete(enrollment);
        log.info("Student ID {} successfully unenrolled from course ID {}.", studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentProfile(Long studentId) {
        log.info("Fetching profile details for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("Profile fetch failed. Student ID {} not found", studentId);
                    return new ResourceNotFoundException("Student profile not found with ID: " + studentId);
                });

        return buildStudentResponse(student);
    }


    @Override
    @Transactional
    public StudentResponse updateStudentProfile(Long studentId, StudentUpdateRequest request) {
        log.info("Updating profile for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("Update failed. Student ID {} not found", studentId);
                    return new ResourceNotFoundException("Student not found with ID: " + studentId);
                });


        if (request.getEmail() != null) {
            student.setEmail(request.getEmail());
        }
        if (request.getMobileNumber() != null) {
            student.setMobileNumber(request.getMobileNumber());
        }
        if (request.getFatherName() != null) {
            student.setFatherName(request.getFatherName());
        }
        if (request.getMotherName() != null) {
            student.setMotherName(request.getMotherName());
        }


        if (request.getAddresses() != null) {
            student.getAddresses().clear();
            request.getAddresses().forEach(dto -> {
                Address address = modelMapper.map(dto, Address.class);
                student.addAddress(address); // sets back-reference too
            });
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Profile updated successfully for student ID: {}", studentId);

        return buildStudentResponse(updatedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> searchStudentsByName(String name, Pageable pageable) {
        log.info("Searching students by name: '{}', page: {}", name, pageable.getPageNumber());

        return studentRepository
                .findByNameContainingIgnoreCase(name, pageable)
                .map(this::buildStudentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudentsByCourse(Long courseId, Pageable pageable) {
        log.info("Fetching students for course ID: {}, page: {}", courseId, pageable.getPageNumber());

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }

        return studentRepository
                .findStudentsByCourseId(courseId, pageable)
                .map(this::buildStudentResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> searchCoursesByName(String courseName, Pageable pageable) {
        log.info("Searching courses by name: '{}', page: {}", courseName, pageable.getPageNumber());

        return courseRepository
                .findByCourseNameContainingIgnoreCase(courseName, pageable)
                .map(this::buildCourseResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> searchCoursesByTopic(String topicName, Pageable pageable) {
        log.info("Searching courses by topic: '{}', page: {}", topicName, pageable.getPageNumber());

        return courseRepository
                .findCoursesByTopicName(topicName, pageable)
                .map(this::buildCourseResponse);
    }

    private StudentResponse buildStudentResponse(Student student) {
        List<String> courseNames = student.getStudentCourses().stream()
                .map(sc -> sc.getCourse().getCourseName())
                .collect(Collectors.toList());

        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .studentCode(student.getStudentCode())
                .email(student.getEmail())
                .enrolledCourses(courseNames)
                .build();
    }

    private CourseResponse buildCourseResponse(Course course) {
        List<String> topicNames = course.getTopics().stream()
                .map(CourseTopic::getTopicName)
                .collect(Collectors.toList());

        return CourseResponse.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .courseType(course.getCourseType())
                .durationMonths(course.getDurationMonths())
                .topics(topicNames)
                .build();
    }
}
