package com.example.employeeapi.employees;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateEmployeeRequest(
        @NotBlank(message = "The employee department is required")
        @Pattern(regexp = "IT|Marketing|HR",
                message = "The employee department must be take one of the following values: 'IT', 'Marketing', 'HR'")
        String department,

        @NotBlank(message = "The employee name is required")
        String name,

        @NotBlank(message = "The employee level is required")
        @Pattern(regexp = "M|J|S",
                message = "The employee level must be take one of the following values: 'M', 'J', 'S'")
        String level) {

    public static CreateEmployeeRequest empty() {
        return new CreateEmployeeRequest(null, null, null);
    }
}
