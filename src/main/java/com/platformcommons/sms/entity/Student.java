package com.platformcommons.sms.entity;

import com.platformcommons.sms.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "student_code", nullable = false, unique = true)
    private String studentCode;

    @Column(name = "email",unique = true)
    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "mother_name")
    private String motherName;


    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();


    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentCourse> studentCourses = new ArrayList<>();

    public void addAddress(Address address) {
        addresses.add(address);
        address.setStudent(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setStudent(null);
    }


}