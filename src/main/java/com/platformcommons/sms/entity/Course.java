package com.platformcommons.sms.entity;

import com.platformcommons.sms.entity.enums.CourseType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false)
    private CourseType courseType;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseTopic> topics = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentCourse> studentCourses = new ArrayList<>();

}