package com.example.employeeapi.employees;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /** Tous les employés, ou seulement ceux du département demandé. */
    public List<Employee> getEmployees(String department) {
        if (StringUtils.hasText(department)) {
            return employeeRepository.findByDepartment(department);
        }
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployee(String employeeId) {
        return employeeRepository.findById(employeeId);
    }

    public void deleteEmployee(String employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    public Employee createEmployee(CreateEmployeeRequest request, String creatorId) {
        Employee employee = new Employee(
                request.name(), request.department(), request.level(), new ObjectId(creatorId));
        return employeeRepository.save(employee);
    }
}
