package com.platformcommons.sms.service;

import com.platformcommons.sms.dto.CourseRequest;
import com.platformcommons.sms.dto.EnrollmentRequest;
import com.platformcommons.sms.dto.StudentRequest;
import com.platformcommons.sms.dto.StudentResponse;
import com.platformcommons.sms.entity.Course;
import com.platformcommons.sms.entity.CourseTopic;
import com.platformcommons.sms.entity.Student;
import com.platformcommons.sms.entity.StudentCourse;
import com.platformcommons.sms.exception.ResourceNotFoundException;
import com.platformcommons.sms.repository.CourseRepository;
import com.platformcommons.sms.repository.StudentCourseRepository;
import com.platformcommons.sms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService{

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public StudentResponse registerStudent(StudentRequest request) {
        log.info("Attempting to register a new student with code: {}", request.getStudentCode());

        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            log.error("Registration failed. Student code {} already exists", request.getStudentCode());
            throw new RuntimeException("Student code already registered in the system!");
        }

        // Map DTO to Entity
        Student student = modelMapper.map(request, Student.class);

        // Map bi-directional back-references for Address mapping
        if (student.getAddresses() != null) {
            student.getAddresses().forEach(address -> address.setStudent(student));
        }

        Student savedStudent = studentRepository.save(student);
        log.info("Student successfully registered with database ID: {}", savedStudent.getId());

        return modelMapper.map(savedStudent, StudentResponse.class);
    }

    @Override
    @Transactional
    public void createCourse(CourseRequest request) {
        log.info("Creating a new course: {}", request.getCourseName());

        Course course = modelMapper.map(request, Course.class);

        // Convert raw string list from request into typed Topic entities bound to the course
        if (request.getTopics() != null) {
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
        log.info("Course successfully saved.");
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

        StudentCourse enrollment = new StudentCourse();
        enrollment.setStudent(student);
        enrollment.setCourse(course);

        studentCourseRepository.save(enrollment);
        log.info("Enrollment entry recorded into junction bridge table mapping successfully.");
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

        List<StudentCourse> enrollments = studentCourseRepository.findByStudentId(studentId);
        List<String> courseNames = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getCourseName())
                .collect(Collectors.toList());

        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .studentCode(student.getStudentCode())
                .email(student.getEmail())
                .enrolledCourses(courseNames)
                .build();
    }
}
