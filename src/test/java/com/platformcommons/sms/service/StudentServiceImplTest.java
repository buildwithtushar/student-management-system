package com.platformcommons.sms.service;

import com.platformcommons.sms.dto.*;
import com.platformcommons.sms.entity.*;
import com.platformcommons.sms.entity.enums.AddressType;
import com.platformcommons.sms.entity.enums.CourseType;
import com.platformcommons.sms.entity.enums.Gender;
import com.platformcommons.sms.exception.DuplicateResourceException;
import com.platformcommons.sms.exception.ResourceNotFoundException;
import com.platformcommons.sms.repository.*;
import com.platformcommons.sms.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("StudentServiceImpl Unit Tests")
class StudentServiceImplTest {


    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentCourseRepository studentCourseRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student  student;
    private Course course;
    private StudentCourse enrollment;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setName("John Doe");
        student.setStudentCode("STU001");
        student.setEmail("john@example.com");
        student.setDateOfBirth(LocalDate.of(2000, 5, 15));
        student.setGender(Gender.MALE);
        student.setStudentCourses(new ArrayList<>());
        student.setAddresses(new ArrayList<>());

        course = new Course();
        course.setId(1L);
        course.setCourseName("Java Basics");
        course.setCourseType(CourseType.CORE);
        course.setDurationMonths(3);
        course.setTopics(new ArrayList<>());
        course.setStudentCourses(new ArrayList<>());

        enrollment = new StudentCourse();
        enrollment.setId(1L);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
    }


    @Nested
    @DisplayName("registerStudent()")
    class RegisterStudent {

        @Test
        @DisplayName("should register student and auto-provision credentials when student code is unique")
        void shouldRegisterStudent_WhenStudentCodeIsUnique() {

            StudentRequest request = buildStudentRequest();
            when(studentRepository.existsByStudentCode("STU001")).thenReturn(false);
            when(modelMapper.map(request, Student.class)).thenReturn(student);
            when(studentRepository.save(student)).thenReturn(student);


            StudentResponse response = studentService.registerStudent(request);


            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("John Doe");
            assertThat(response.getStudentCode()).isEqualTo("STU001");
            assertThat(response.getEmail()).isEqualTo("john@example.com");

            verify(studentRepository).existsByStudentCode("STU001");
            verify(studentRepository).save(student);
            verify(userCredentialRepository).save(any(UserCredential.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when student code already exists")
        void shouldThrowDuplicateResourceException_WhenStudentCodeAlreadyExists() {

            StudentRequest request = buildStudentRequest();
            when(studentRepository.existsByStudentCode("STU001")).thenReturn(true);


            assertThatThrownBy(() -> studentService.registerStudent(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("STU001");

            verify(studentRepository, never()).save(any());
            verify(userCredentialRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set student back-reference on each address before saving")
        void shouldSetStudentBackReferenceOnAddresses() {

            StudentRequest request = buildStudentRequest();
            Address address = new Address();
            student.setAddresses(List.of(address));

            when(studentRepository.existsByStudentCode("STU001")).thenReturn(false);
            when(modelMapper.map(request, Student.class)).thenReturn(student);
            when(studentRepository.save(student)).thenReturn(student);

            studentService.registerStudent(request);


            assertThat(address.getStudent()).isEqualTo(student);
        }

        @Test
        @DisplayName("should save UserCredential with ROLE_STUDENT after student is saved")
        void shouldSaveUserCredentialWithRoleStudent() {

            StudentRequest request = buildStudentRequest();
            when(studentRepository.existsByStudentCode("STU001")).thenReturn(false);
            when(modelMapper.map(request, Student.class)).thenReturn(student);
            when(studentRepository.save(student)).thenReturn(student);

            studentService.registerStudent(request);

            ArgumentCaptor<UserCredential> credCaptor = ArgumentCaptor.forClass(UserCredential.class);
            verify(userCredentialRepository).save(credCaptor.capture());
            UserCredential saved = credCaptor.getValue();
            assertThat(saved.getUsername()).isEqualTo("STU001");
            assertThat(saved.getRole()).isEqualTo("ROLE_STUDENT");
            assertThat(saved.getPassword()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("getStudentProfile()")
    class GetStudentProfile {

        @Test
        @DisplayName("should return student response when student exists")
        void shouldReturnStudentResponse_WhenStudentExists() {

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));


            StudentResponse response = studentService.getStudentProfile(1L);


            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("John Doe");
            assertThat(response.getStudentCode()).isEqualTo("STU001");
            assertThat(response.getEnrolledCourses()).isEmpty();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when student does not exist")
        void shouldThrowResourceNotFoundException_WhenStudentNotFound() {

            when(studentRepository.findById(99L)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> studentService.getStudentProfile(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("should include enrolled course names in response")
        void shouldIncludeEnrolledCourseNames_WhenStudentHasEnrollments() {

            student.getStudentCourses().add(enrollment);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            StudentResponse response = studentService.getStudentProfile(1L);

            assertThat(response.getEnrolledCourses()).containsExactly("Java Basics");
        }
    }


    @Nested
    @DisplayName("updateStudentProfile()")
    class UpdateStudentProfile {

        @Test
        @DisplayName("should update email and mobile when both are provided")
        void shouldUpdateEmailAndMobile_WhenProvided() {

            StudentUpdateRequest request = new StudentUpdateRequest();
            request.setEmail("new@example.com");
            request.setMobileNumber("9999999999");

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(studentRepository.save(student)).thenReturn(student);

            StudentResponse response = studentService.updateStudentProfile(1L, request);


            assertThat(response.getEmail()).isEqualTo("new@example.com");
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("should not overwrite email when not provided in request")
        void shouldNotOverwriteEmail_WhenNotProvided() {

            StudentUpdateRequest request = new StudentUpdateRequest();
            request.setMobileNumber("9999999999");

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(studentRepository.save(student)).thenReturn(student);


            studentService.updateStudentProfile(1L, request);

            assertThat(student.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("should replace all addresses when addresses list is provided")
        void shouldReplaceAddresses_WhenAddressesProvided() {

            StudentUpdateRequest.AddressDto dto = new StudentUpdateRequest.AddressDto();
            dto.setStreet("New St");
            dto.setCity("Pune");
            dto.setState("MH");
            dto.setZipCode("411001");
            dto.setAddressType(AddressType.CURRENT);

            StudentUpdateRequest request = new StudentUpdateRequest();
            request.setAddresses(List.of(dto));

            Address mappedAddress = new Address();
            mappedAddress.setCity("Pune");

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(modelMapper.map(dto, Address.class)).thenReturn(mappedAddress);
            when(studentRepository.save(student)).thenReturn(student);

            studentService.updateStudentProfile(1L, request);

            assertThat(student.getAddresses()).hasSize(1);
            assertThat(student.getAddresses().get(0).getCity()).isEqualTo("Pune");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when student does not exist")
        void shouldThrow_WhenStudentNotFound() {

            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.updateStudentProfile(99L, new StudentUpdateRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }


    @Nested
    @DisplayName("createCourse()")
    class CreateCourse {

        @Test
        @DisplayName("should save course with mapped topics when topics list is provided")
        void shouldSaveCourseWithTopics_WhenTopicsProvided() {

            CourseRequest request = new CourseRequest();
            request.setCourseName("Java Basics");
            request.setCourseType(CourseType.CORE);
            request.setDurationMonths(3);
            request.setTopics(List.of("OOP", "Collections", "Streams"));

            when(modelMapper.map(request, Course.class)).thenReturn(course);

            studentService.createCourse(request);

            verify(courseRepository).save(course);
            assertThat(course.getTopics()).hasSize(3);
            assertThat(course.getTopics()).extracting(CourseTopic::getTopicName)
                    .containsExactlyInAnyOrder("OOP", "Collections", "Streams");
        }

        @Test
        @DisplayName("should save course with empty topics when topics list is null")
        void shouldSaveCourse_WhenTopicsNull() {

            CourseRequest request = new CourseRequest();
            request.setCourseName("Java Basics");
            request.setCourseType(CourseType.CORE);
            request.setDurationMonths(3);
            request.setTopics(null);

            when(modelMapper.map(request, Course.class)).thenReturn(course);

            studentService.createCourse(request);

            verify(courseRepository).save(course);
            assertThat(course.getTopics()).isEmpty();
        }
    }

    @Nested
    @DisplayName("enrollStudentInCourse()")
    class EnrollStudentInCourse {

        @Test
        @DisplayName("should enroll student when both exist and not already enrolled")
        void shouldEnrollStudent_WhenNotAlreadyEnrolled() {

            EnrollmentRequest request = new EnrollmentRequest();
            request.setStudentId(1L);
            request.setCourseId(1L);

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(studentCourseRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(false);


            studentService.enrollStudentInCourse(request);


            ArgumentCaptor<StudentCourse> captor = ArgumentCaptor.forClass(StudentCourse.class);
            verify(studentCourseRepository).save(captor.capture());
            assertThat(captor.getValue().getStudent()).isEqualTo(student);
            assertThat(captor.getValue().getCourse()).isEqualTo(course);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when already enrolled")
        void shouldThrowDuplicateResourceException_WhenAlreadyEnrolled() {

            EnrollmentRequest request = new EnrollmentRequest();
            request.setStudentId(1L);
            request.setCourseId(1L);

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(studentCourseRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(true);


            assertThatThrownBy(() -> studentService.enrollStudentInCourse(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(studentCourseRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when student does not exist")
        void shouldThrow_WhenStudentNotFound() {

            EnrollmentRequest request = new EnrollmentRequest();
            request.setStudentId(99L);
            request.setCourseId(1L);

            when(studentRepository.findById(99L)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> studentService.enrollStudentInCourse(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when course does not exist")
        void shouldThrow_WhenCourseNotFound() {

            EnrollmentRequest request = new EnrollmentRequest();
            request.setStudentId(1L);
            request.setCourseId(99L);

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> studentService.enrollStudentInCourse(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }


    @Nested
    @DisplayName("unenrollStudentFromCourse()")
    class UnenrollStudentFromCourse {

        @Test
        @DisplayName("should delete enrollment when it exists")
        void shouldDeleteEnrollment_WhenExists() {

            when(studentCourseRepository.findByStudentIdAndCourseId(1L, 1L))
                    .thenReturn(Optional.of(enrollment));


            studentService.unenrollStudentFromCourse(1L, 1L);


            verify(studentCourseRepository).delete(enrollment);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when enrollment does not exist")
        void shouldThrow_WhenEnrollmentNotFound() {

            when(studentCourseRepository.findByStudentIdAndCourseId(1L, 99L))
                    .thenReturn(Optional.empty());


            assertThatThrownBy(() -> studentService.unenrollStudentFromCourse(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(studentCourseRepository, never()).delete(any());
        }
    }


    @Nested
    @DisplayName("searchStudentsByName()")
    class SearchStudentsByName {

        @Test
        @DisplayName("should return paginated student responses matching the name")
        void shouldReturnPagedStudents_WhenNameMatches() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);
            when(studentRepository.findByNameContainingIgnoreCase("John", pageable)).thenReturn(page);


            Page<StudentResponse> result = studentService.searchStudentsByName("John", pageable);


            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("should return empty page when no students match the name")
        void shouldReturnEmptyPage_WhenNoMatch() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> emptyPage = Page.empty(pageable);
            when(studentRepository.findByNameContainingIgnoreCase("xyz", pageable)).thenReturn(emptyPage);


            Page<StudentResponse> result = studentService.searchStudentsByName("xyz", pageable);


            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }
    }


    @Nested
    @DisplayName("getStudentsByCourse()")
    class GetStudentsByCourse {

        @Test
        @DisplayName("should return paginated students enrolled in course")
        void shouldReturnPagedStudents_WhenCourseExists() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);
            when(courseRepository.existsById(1L)).thenReturn(true);
            when(studentRepository.findStudentsByCourseId(1L, pageable)).thenReturn(page);


            Page<StudentResponse> result = studentService.getStudentsByCourse(1L, pageable);


            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when course does not exist")
        void shouldThrow_WhenCourseNotFound() {

            Pageable pageable = PageRequest.of(0, 10);
            when(courseRepository.existsById(99L)).thenReturn(false);


            assertThatThrownBy(() -> studentService.getStudentsByCourse(99L, pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }


    @Nested
    @DisplayName("searchCoursesByName()")
    class SearchCoursesByName {

        @Test
        @DisplayName("should return paginated course responses matching the course name")
        void shouldReturnPagedCourses_WhenNameMatches() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<Course> page = new PageImpl<>(List.of(course), pageable, 1);
            when(courseRepository.findByCourseNameContainingIgnoreCase("Java", pageable)).thenReturn(page);


            Page<CourseResponse> result = studentService.searchCoursesByName("Java", pageable);


            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getCourseName()).isEqualTo("Java Basics");
        }
    }


    @Nested
    @DisplayName("searchCoursesByTopic()")
    class SearchCoursesByTopic {

        @Test
        @DisplayName("should return courses containing the given topic")
        void shouldReturnCourses_WhenTopicMatches() {

            CourseTopic topic = new CourseTopic();
            topic.setTopicName("OOP");
            topic.setCourse(course);
            course.getTopics().add(topic);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Course> page = new PageImpl<>(List.of(course), pageable, 1);
            when(courseRepository.findCoursesByTopicName("OOP", pageable)).thenReturn(page);


            Page<CourseResponse> result = studentService.searchCoursesByTopic("OOP", pageable);


            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTopics()).contains("OOP");
        }

        @Test
        @DisplayName("should return empty page when no course matches the topic")
        void shouldReturnEmptyPage_WhenTopicNotFound() {

            Pageable pageable = PageRequest.of(0, 10);
            when(courseRepository.findCoursesByTopicName("NonExistentTopic", pageable))
                    .thenReturn(Page.empty(pageable));


            Page<CourseResponse> result = studentService.searchCoursesByTopic("NonExistentTopic", pageable);


            assertThat(result).isEmpty();
        }
    }


    private StudentRequest buildStudentRequest() {
        StudentRequest request = new StudentRequest();
        request.setName("John Doe");
        request.setDateOfBirth(LocalDate.of(2000, 5, 15));
        request.setGender(Gender.MALE);
        request.setStudentCode("STU001");
        request.setEmail("john@example.com");
        request.setMobileNumber("9876543210");

        StudentRequest.AddressDto addr = new StudentRequest.AddressDto();
        addr.setStreet("123 Main St");
        addr.setCity("Mumbai");
        addr.setState("Maharashtra");
        addr.setZipCode("400001");
        addr.setAddressType(AddressType.CURRENT);
        request.setAddresses(List.of(addr));

        return request;
    }
}
