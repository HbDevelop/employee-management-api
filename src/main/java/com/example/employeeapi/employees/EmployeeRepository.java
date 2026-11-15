package com.example.employeeapi.employees;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

/** Accès aux employés en base. */
public interface EmployeeRepository extends MongoRepository<Employee, String> {

    List<Employee> findByDepartment(String department);
}
