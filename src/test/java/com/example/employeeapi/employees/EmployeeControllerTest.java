package com.example.employeeapi.employees;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.example.employeeapi.authentication.AuthUser;
import com.example.employeeapi.core.error.GlobalExceptionHandler;
import com.example.employeeapi.core.validation.RequestValidator;
import jakarta.validation.Validation;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EmployeeControllerTest {

    private static final String CONNECTED_USER_ID = new ObjectId().toHexString();

    private EmployeeService employeeService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        employeeService = mock(EmployeeService.class);
        var validator = new RequestValidator(Validation.buildDefaultValidatorFactory().getValidator());
        mockMvc = MockMvcBuilders.standaloneSetup(new EmployeeController(employeeService, validator))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        var connectedUser = new AuthUser(CONNECTED_USER_ID, "ada@example.com", "Ada", "Lovelace");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(connectedUser, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static Employee employee(String id, String createdBy) {
        Employee employee = new Employee("Ada", "IT", "S", new ObjectId(createdBy));
        employee.setId(id);
        return employee;
    }

    @Test
    void getAllEmployeesPassesTheDepartmentFilter() throws Exception {
        String id = new ObjectId().toHexString();
        when(employeeService.getEmployees("IT")).thenReturn(List.of(employee(id, CONNECTED_USER_ID)));

        mockMvc.perform(get("/employees").param("department", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]._id").value(id))
                .andExpect(jsonPath("$[0].name").value("Ada"))
                .andExpect(jsonPath("$[0].createdBy").value(CONNECTED_USER_ID));
    }

    @Test
    void getEmployeeByIdReturns404WhenIdIsNotAnObjectId() throws Exception {
        mockMvc.perform(get("/employees/not-an-id")).andExpect(status().isNotFound());

        verify(employeeService, never()).getEmployee(any());
    }

    @Test
    void getEmployeeByIdReturns404WhenEmployeeDoesNotExist() throws Exception {
        String id = new ObjectId().toHexString();
        when(employeeService.getEmployee(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeByIdReturnsTheEmployee() throws Exception {
        String id = new ObjectId().toHexString();
        when(employeeService.getEmployee(id)).thenReturn(Optional.of(employee(id, CONNECTED_USER_ID)));

        mockMvc.perform(get("/employees/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value(id))
                .andExpect(jsonPath("$.department").value("IT"));
    }

    @Test
    void deleteEmployeeReturns403WhenConnectedUserIsNotTheCreator() throws Exception {
        String id = new ObjectId().toHexString();
        String anotherUserId = new ObjectId().toHexString();
        when(employeeService.getEmployee(id)).thenReturn(Optional.of(employee(id, anotherUserId)));

        mockMvc.perform(delete("/employees/" + id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only creator of an employee can delete that employee."));

        verify(employeeService, never()).deleteEmployee(any());
    }

    @Test
    void deleteEmployeeReturns204WhenConnectedUserIsTheCreator() throws Exception {
        String id = new ObjectId().toHexString();
        when(employeeService.getEmployee(id)).thenReturn(Optional.of(employee(id, CONNECTED_USER_ID)));

        mockMvc.perform(delete("/employees/" + id)).andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee(id);
    }

    @Test
    void createEmployeeReturnsValidationErrorsWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/employees"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].field").value("department"))
                .andExpect(jsonPath("$[0].error").value("The employee department is required"))
                .andExpect(jsonPath("$[1].field").value("name"))
                .andExpect(jsonPath("$[2].field").value("level"));
    }

    @Test
    void createEmployeeRejectsUnknownDepartmentAndLevel() throws Exception {
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"department\":\"Sales\",\"name\":\"Ada\",\"level\":\"X\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].field").value("department"))
                .andExpect(jsonPath("$[1].field").value("level"));
    }

    @Test
    void createEmployeeReturns201WithTheCreatedEmployee() throws Exception {
        String id = new ObjectId().toHexString();
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class), eq(CONNECTED_USER_ID)))
                .thenReturn(employee(id, CONNECTED_USER_ID));

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"department\":\"IT\",\"name\":\"Ada\",\"level\":\"S\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._id").value(id))
                .andExpect(jsonPath("$.createdBy").value(CONNECTED_USER_ID));
    }
}
