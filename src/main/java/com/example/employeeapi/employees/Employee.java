package com.example.employeeapi.employees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Un employé, tel qu'enregistré en base. */
@Document(collection = "employees")
public class Employee {

    @Id
    @JsonProperty("_id")
    private String id;

    private String name;

    /** "IT", "Marketing" ou "HR". */
    private String department;

    /** "J" (junior), "M" (medior) ou "S" (senior). */
    private String level;

    /** Identifiant de l'utilisateur qui a créé l'employé. */
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId createdBy;

    public Employee() {
    }

    public Employee(String name, String department, String level, ObjectId createdBy) {
        this.name = name;
        this.department = department;
        this.level = level;
        this.createdBy = createdBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public ObjectId getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ObjectId createdBy) {
        this.createdBy = createdBy;
    }
}
