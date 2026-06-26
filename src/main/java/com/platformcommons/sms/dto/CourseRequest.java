package com.platformcommons.sms.dto;

import com.platformcommons.sms.entity.enums.CourseType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CourseRequest {
    @NotBlank(message = "courseName must not be blank")
    private String courseName;

    private String description;

    @NotNull(message = "courseType must not be null")
    private CourseType courseType;

    @NotNull(message = "durationMonths must not be null")
    @Min(value = 1, message = "durationMonths must be at least 1")
    private Integer durationMonths;

    private List<String> topics;
}

