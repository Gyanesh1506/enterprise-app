package com.enterprise.app.service;

import com.enterprise.app.entity.Employee;
import com.enterprise.app.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    // Test 1: Get all employees
    @Test
    void getAllEmployees_shouldReturnAllEmployees() {

        Employee employee = new Employee();
        employee.setName("Rahul Sharma");
        employee.setEmail("rahul@example.com");
        employee.setDepartment("Engineering");
        employee.setSalary(65000.0);

        when(employeeRepository.findAll())
                .thenReturn(List.of(employee));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("Rahul Sharma", result.get(0).getName());

        verify(employeeRepository, times(1)).findAll();
    }

    // Test 2: Save an employee
    @Test
    void saveEmployee_shouldSaveAndReturnEmployee() {

        Employee employee = new Employee();
        employee.setName("Amit Verma");
        employee.setEmail("amit@example.com");
        employee.setDepartment("IT");
        employee.setSalary(55000.0);

        when(employeeRepository.save(employee))
                .thenReturn(employee);

        Employee result = employeeService.saveEmployee(employee);

        assertNotNull(result);
        assertEquals("Amit Verma", result.getName());
        assertEquals("amit@example.com", result.getEmail());

        verify(employeeRepository, times(1)).save(employee);
    }

    // Test 3: Get employee by ID
    @Test
    void getEmployeeById_shouldReturnEmployee() {

        Employee employee = new Employee();
        employee.setName("Vikram Singh");
        employee.setEmail("vikram@example.com");
        employee.setDepartment("DevOps");
        employee.setSalary(70000.0);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals("Vikram Singh", result.getName());

        verify(employeeRepository, times(1)).findById(1L);
    }

    // Test 4: Employee not found
    @Test
    void getEmployeeById_shouldThrowExceptionWhenNotFound() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.getEmployeeById(99L)
        );

        assertEquals("Employee not found", exception.getMessage());

        verify(employeeRepository, times(1)).findById(99L);
    }

    // Test 5: Update employee
    @Test
    void updateEmployee_shouldUpdateAndReturnEmployee() {

        Employee existing = new Employee();
        existing.setName("Rahul Sharma");
        existing.setEmail("rahul@example.com");
        existing.setDepartment("Engineering");
        existing.setSalary(65000.0);

        Employee updated = new Employee();
        updated.setName("Rahul Sharma");
        updated.setEmail("rahul@example.com");
        updated.setDepartment("DevOps");
        updated.setSalary(75000.0);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(employeeRepository.save(existing))
                .thenReturn(existing);

        Employee result = employeeService.updateEmployee(1L, updated);

        assertEquals("DevOps", result.getDepartment());
        assertEquals(75000.0, result.getSalary());

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(existing);
    }

    // Test 6: Delete an existing employee
    @Test
    void deleteEmployee_shouldDeleteEmployee() {

        Employee employee = new Employee();
        employee.setName("Rahul Sharma");

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).delete(employee);
    }

    // Test 7: Try to delete an employee who does not exist
    @Test
    void deleteEmployee_shouldThrowExceptionWhenNotFound() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.deleteEmployee(99L)
        );

        assertEquals("Employee not found", exception.getMessage());

        verify(employeeRepository, times(1)).findById(99L);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }
}
