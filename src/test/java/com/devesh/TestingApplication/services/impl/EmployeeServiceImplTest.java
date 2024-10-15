package com.devesh.TestingApplication.services.impl;

import com.devesh.TestingApplication.TestContainerConfiguration;
import com.devesh.TestingApplication.dto.EmployeeDto;
import com.devesh.TestingApplication.entities.Employee;
import com.devesh.TestingApplication.exceptions.ResourceNotFoundException;
import com.devesh.TestingApplication.repositories.EmployeeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Import(TestContainerConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee mockEmployee;
    private EmployeeDto mockEmployeeDto;

    @BeforeEach
    void setUp(){
        Long id=1L;
        mockEmployee=Employee.builder()
                .id(id)
                .name("Dev")
                .email("dev@gmail.com")
                .salary(100L)
                .build();
        mockEmployeeDto=modelMapper.map(mockEmployee,EmployeeDto.class);
    }

    @Test
    void testGetEmployeeById_WhenEmployeeIdIsPresent_ThenReturnEmployeeDto(){
        // assign
        Long id=mockEmployee.getId();
        when(employeeRepository.findById(id)).thenReturn(Optional.of(mockEmployee)); // Stubbing

        // act
        EmployeeDto employeeDto=employeeService.getEmployeeById(id);

        // assert
        Assertions.assertThat(employeeDto).isNotNull();
        Assertions.assertThat(employeeDto.getId()).isEqualTo(id);
        Assertions.assertThat(employeeDto.getEmail()).isEqualTo(mockEmployee.getEmail());
        verify(employeeRepository,only()).findById(id);
    }

    @Test
    void testGetEmployeeById_whenEmployeeIsNotPresent_thenThrowException(){
        // arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        // act and assert
        Assertions.assertThatThrownBy(()->employeeService.getEmployeeById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: 1");
        verify(employeeRepository).findById(1L);
    }

    @Test
    void testCreateNewEmployee_WhenValidEmployee_ThenCreateNewEmployee(){
        // assign
        when(employeeRepository.findByEmail(anyString())).thenReturn(List.of());
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        // act
        EmployeeDto employeeDto=employeeService.createNewEmployee(mockEmployeeDto);

        // assert
        Assertions.assertThat(employeeDto).isNotNull();
        Assertions.assertThat(employeeDto.getEmail()).isEqualTo(mockEmployeeDto.getEmail());

        // Argument Captor
        ArgumentCaptor<Employee> employeeArgumentCaptor=ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeArgumentCaptor.capture());

        Employee capturedEmployee=employeeArgumentCaptor.getValue();
        Assertions.assertThat(capturedEmployee.getEmail()).isEqualTo(mockEmployee.getEmail());
    }

    @Test
    void testCreateNewEmployee_whenAttemptingToCreateEmployeeWithExistingEmail_thenThrowException(){
        // assign
        when(employeeRepository.findByEmail(mockEmployeeDto.getEmail())).thenReturn(List.of(mockEmployee));
        //act and assert
        Assertions.assertThatThrownBy(()->employeeService.createNewEmployee(mockEmployeeDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Employee already exists with email: "+mockEmployee.getEmail());
        verify(employeeRepository).findByEmail(mockEmployeeDto.getEmail());
        verify(employeeRepository,never()).save(any());
    }

    @Test
    void testUpdateEmployee_whenEmployeeDoesNotExists_theThrowException(){
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // act and assert
        Assertions.assertThatThrownBy(()->employeeService.updateEmployee(1L,mockEmployeeDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: 1");
        verify(employeeRepository).findById(1L);
        verify(employeeRepository,never()).save(any());

    }

    @Test
    void testUpdateEmployee_whenAttemptingToUpdateEmail_thenThroeException(){
        // Assign
        when(employeeRepository.findById(mockEmployeeDto.getId())).thenReturn(Optional.of(mockEmployee));
        mockEmployeeDto.setName("Random");
        mockEmployeeDto.setEmail("random@gmail.com");

        // act and assert
        assertThatThrownBy(()->employeeService.updateEmployee(mockEmployeeDto.getId(),mockEmployeeDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("The email of the employee cannot be updated");
        verify(employeeRepository).findById(mockEmployeeDto.getId());
        verify(employeeRepository,never()).save(any());
    }

    @Test
    void testUpdateEmployee_whenValidEmployee_thenUpdateEmployee(){
        // assign
        when(employeeRepository.findById(mockEmployeeDto.getId())).thenReturn(Optional.of(mockEmployee));
        mockEmployeeDto.setName("Random");
        mockEmployeeDto.setSalary(199L);
        Employee newEmployee=modelMapper.map(mockEmployeeDto, Employee.class);
        when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployee);

        // act and assert
        EmployeeDto updatedEmployeeDto=employeeService.updateEmployee(mockEmployeeDto.getId(),mockEmployeeDto);
        Assertions.assertThat(updatedEmployeeDto).isEqualTo(mockEmployeeDto);
        verify(employeeRepository).findById(mockEmployeeDto.getId());
        verify(employeeRepository).save(any());
    }

    @Test
    void testDeleteEmployee_whenEmployeeDoesNotExists_thenThroeException(){
        // assign
        when(employeeRepository.existsById(mockEmployeeDto.getId())).thenReturn(false);

        //act and assert
        assertThatThrownBy(()->employeeService.deleteEmployee(mockEmployeeDto.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: "+mockEmployeeDto.getId());
        verify(employeeRepository).existsById(mockEmployeeDto.getId());
        verify(employeeRepository,never()).deleteById(mockEmployeeDto.getId());
    }

    @Test
    void testDeleteEmployee_whenEmployeeIsValid_thenDeleteEmployee(){
        // assign
        when(employeeRepository.existsById(mockEmployeeDto.getId())).thenReturn(true);

        // act and assert
        assertThatCode(()->employeeService.deleteEmployee(mockEmployeeDto.getId()))
                .doesNotThrowAnyException();
        verify(employeeRepository).deleteById(mockEmployeeDto.getId());
    }
}