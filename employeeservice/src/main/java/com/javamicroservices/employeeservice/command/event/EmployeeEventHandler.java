package com.javamicroservices.employeeservice.command.event;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.employeeservice.command.data.Employee;
import com.javamicroservices.employeeservice.command.data.EmployeeRepository;

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
}
