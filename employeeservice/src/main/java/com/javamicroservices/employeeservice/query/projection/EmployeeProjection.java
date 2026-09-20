package com.javamicroservices.employeeservice.query.projection;

import java.util.List;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.employeeservice.command.data.Employee;
import com.javamicroservices.employeeservice.command.data.EmployeeRepository;
import com.javamicroservices.employeeservice.query.model.EmployeeResponseModel;
import com.javamicroservices.employeeservice.query.queries.GetAllEmployeeQuery;
import com.javamicroservices.employeeservice.query.queries.GetDetailEmployeeQuery;

@Component 
public class EmployeeProjection {
    @Autowired 
    private EmployeeRepository employeeRepository;

    @QueryHandler 
    public List<EmployeeResponseModel> handle(GetAllEmployeeQuery query) {
        List<Employee> employees = employeeRepository.findByIsDisciplined(query.getIsDisciplined());
        return employees.stream().map(employee -> {
            EmployeeResponseModel model = new EmployeeResponseModel();
            BeanUtils.copyProperties(employee, model);
            return model;
        }).toList();
    }

    @QueryHandler
    public EmployeeResponseModel handle(GetDetailEmployeeQuery query) throws Exception {
        Employee employee = employeeRepository.findById(query.getId()).orElseThrow(() -> new Exception("Employee not found"));
        EmployeeResponseModel model = new EmployeeResponseModel();
        BeanUtils.copyProperties(employee, model);
        return model;
    }
}
