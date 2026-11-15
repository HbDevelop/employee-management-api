package com.example.employeeapi.employees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void getEmployeesReturnsAllEmployeesWhenNoDepartmentIsGiven() {
        List<Employee> all = List.of(new Employee("Ada", "IT", "S", null), new Employee("Bob", "HR", "J", null));
        when(employeeRepository.findAll()).thenReturn(all);

        assertThat(employeeService.getEmployees(null)).isEqualTo(all);
        verify(employeeRepository, never()).findByDepartment(any());
    }

    @Test
    void getEmployeesFiltersByDepartment() {
        List<Employee> itEmployees = List.of(new Employee("Ada", "IT", "S", null));
        when(employeeRepository.findByDepartment("IT")).thenReturn(itEmployees);

        assertThat(employeeService.getEmployees("IT")).isEqualTo(itEmployees);
        verify(employeeRepository, never()).findAll();
    }

    @Test
    void getEmployeeDelegatesToRepository() {
        String id = new ObjectId().toHexString();
        Employee employee = new Employee("Ada", "IT", "S", null);
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        assertThat(employeeService.getEmployee(id)).contains(employee);
    }

    @Test
    void deleteEmployeeDelegatesToRepository() {
        String id = new ObjectId().toHexString();

        employeeService.deleteEmployee(id);

        verify(employeeRepository).deleteById(id);
    }

    @Test
    void createEmployeeStoresTheCreator() {
        String creatorId = new ObjectId().toHexString();
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee created = employeeService.createEmployee(new CreateEmployeeRequest("IT", "Ada", "S"), creatorId);

        ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Ada");
        assertThat(saved.getValue().getDepartment()).isEqualTo("IT");
        assertThat(saved.getValue().getLevel()).isEqualTo("S");
        assertThat(saved.getValue().getCreatedBy().toHexString()).isEqualTo(creatorId);
        assertThat(created).isSameAs(saved.getValue());
    }
}
