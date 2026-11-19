
package com.enterprise.app.controller;

import com.enterprise.app.entity.Employee;
import com.enterprise.app.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Smith");
        employee.setEmail("john@example.com");
        employee.setDepartment("IT");
        employee.setSalary(50000.0);
    }

    @Test
    void createEmployeeShouldReturnSavedEmployee() {
        when(employeeService.saveEmployee(employee)).thenReturn(employee);

        Employee result = employeeController.createEmployee(employee);

        assertEquals(employee, result);
        verify(employeeService).saveEmployee(employee);
    }

    @Test
    void getAllEmployeesShouldReturnEmployeeList() {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee));

        List<Employee> result = employeeController.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals(employee, result.get(0));
        verify(employeeService).getAllEmployees();
    }

    @Test
    void getEmployeeByIdShouldReturnEmployee() {
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        Employee result = employeeController.getEmployeeById(1L);

        assertEquals(employee, result);
        verify(employeeService).getEmployeeById(1L);
    }

    @Test
    void updateEmployeeShouldReturnUpdatedEmployee() {
        when(employeeService.updateEmployee(1L, employee)).thenReturn(employee);

        Employee result = employeeController.updateEmployee(1L, employee);

        assertEquals(employee, result);
        verify(employeeService).updateEmployee(1L, employee);
    }

    @Test
    void deleteEmployeeShouldCallService() {
        employeeController.deleteEmployee(1L);

        verify(employeeService).deleteEmployee(1L);
    }
}
