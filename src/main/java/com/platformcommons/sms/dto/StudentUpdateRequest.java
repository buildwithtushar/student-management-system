package com.platformcommons.sms.dto;

import com.platformcommons.sms.entity.enums.AddressType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StudentUpdateRequest {

    @Email(message = "must be a valid email address")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "must be a 10-digit mobile number")
    private String mobileNumber;

    private String fatherName;
    private String motherName;

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

        private AddressType addressType;
    }

}
