package com.policy.api.controller;

import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.service.CustomerService;
import com.policy.api.dto.request.CustomerRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    public CustomerResponse createCustomer(@RequestBody CustomerRequest customer) {
        return service.createCustomer(customer);
    }

    @GetMapping("/")
    public List<CustomerResponse> getAllCustomers(){
        return service.getAllCustomers();
    }

    @GetMapping("/{customerID}")
    public CustomerResponse getCustomer(@PathVariable String customerID){
        return service.getCustomer(customerID);
    }

    @PutMapping("/{customerID}")
    public CustomerResponse updateCustomer(@PathVariable String customerID, @RequestBody CustomerRequest customer){
        return service.updateCustomer(customerID, customer);
    }
}
