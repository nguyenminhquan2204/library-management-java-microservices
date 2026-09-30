package com.javamicroservices.borrowingservice.query.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class GetBorrowingWithEmployeeIdQuery {
    private String employeeId;
}
