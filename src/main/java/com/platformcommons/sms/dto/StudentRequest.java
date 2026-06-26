package com.platformcommons.sms.dto;

import com.platformcommons.sms.entity.enums.AddressType;
import com.platformcommons.sms.entity.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class StudentRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "dateOfBirth must not be null")
    @Past(message = "dateOfBirth must be a past date")
    private LocalDate dateOfBirth;

    @NotNull(message = "gender must not be null")
    private Gender gender;

    @NotBlank(message = "studentCode must not be blank")
    private String studentCode;

    @Email(message = "must be a valid email address")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "must be a 10-digit mobile number")
    private String mobileNumber;

    private String fatherName;

    private String motherName;

    @NotEmpty(message = "at least one address must be provided")
    @Valid
    private List<AddressDto> addresses;

    @Getter
    @Setter
    public static class AddressDto {

        @NotBlank(message = "street must not be blank")
        private String street;

        @NotBlank(message = "city must not be blank")
        private String city;

        @NotBlank(message = "state must not be blank")
        private String state;

        @NotBlank(message = "zipCode must not be blank")
        private String zipCode;

        @NotNull(message = "addressType must not be null")
        private AddressType addressType;
    }
}
