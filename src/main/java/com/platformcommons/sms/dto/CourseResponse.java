package com.platformcommons.sms.dto;

import com.platformcommons.sms.entity.enums.CourseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CourseResponse {
    private Long id;
    private String courseName;
    private String description;
    private CourseType courseType;
    private Integer durationMonths;
    private List<String> topics;
}
