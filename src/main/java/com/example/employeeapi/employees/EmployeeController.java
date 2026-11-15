package com.example.employeeapi.employees;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.employeeapi.authentication.AuthUser;
import com.example.employeeapi.core.validation.RequestValidator;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Routes des employés. Toutes demandent d'être connecté. */
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final RequestValidator requestValidator;

    public EmployeeController(EmployeeService employeeService, RequestValidator requestValidator) {
        this.employeeService = employeeService;
        this.requestValidator = requestValidator;
    }

    /** Liste les employés, avec un filtre optionnel : /employees?department=IT */
    @GetMapping
    public List<Employee> getAllEmployees(@RequestParam(required = false) String department) {
        return employeeService.getEmployees(department);
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String employeeId) {
        if (!ObjectId.isValid(employeeId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(employeeService.getEmployee(employeeId));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Object> deleteEmployee(@PathVariable String employeeId,
                                                 @AuthenticationPrincipal AuthUser connectedUser) {
        if (!ObjectId.isValid(employeeId)) {
            return ResponseEntity.notFound().build();
        }
        Optional<Employee> employee = employeeService.getEmployee(employeeId);
        if (employee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Seul le créateur de l'employé peut le supprimer
        ObjectId createdBy = employee.get().getCreatedBy();
        if (createdBy == null || !createdBy.toHexString().equals(connectedUser.id())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only creator of an employee can delete that employee."));
        }
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody(required = false) CreateEmployeeRequest body,
                                                   @AuthenticationPrincipal AuthUser connectedUser) {
        CreateEmployeeRequest request = body != null ? body : CreateEmployeeRequest.empty();
        requestValidator.validate(request);

        Employee employee = employeeService.createEmployee(request, connectedUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }
}
