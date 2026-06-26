package com.platformcommons.sms.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class StudentResponse {
    private Long id;
    private String name;
    private String studentCode;
    private String email;
    private List<String> enrolledCourses;
}
