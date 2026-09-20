package com.javamicroservices.employeeservice.command.event;

import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.employeeservice.command.data.Employee;
import com.javamicroservices.employeeservice.command.data.EmployeeRepository;

import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
public class EmployeeEventHandler {
    @Autowired 
    private EmployeeRepository employeeRepository;

    @EventHandler 
    public void on(EmployeeCreatedEvent event) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(event, employee);
        employeeRepository.save(employee);
    }

    @EventHandler 
    public void on(EmployeeUpdatedEvent event) {
        Employee employee = employeeRepository.findById(event.getId()).orElseThrow(() -> new NotFoundException("Employee not found"));
        BeanUtils.copyProperties(event, employee);
        employeeRepository.save(employee);
    }

    @EventHandler 
    @DisallowReplay 
    public void on(EmployeeDeletedEvent event) {
        try {
            Employee employee = employeeRepository.findById(event.getId()).orElseThrow(() -> new Exception("Employee not found"));
            employeeRepository.delete(employee);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
