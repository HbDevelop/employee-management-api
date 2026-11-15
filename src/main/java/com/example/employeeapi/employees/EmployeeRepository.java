package com.example.employeeapi.employees;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmployeeRepository extends MongoRepository<Employee, String> {

    List<Employee> findByDepartment(String department);
}
