package com.policy.api.controller;

import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.service.CustomerService;
import com.policy.api.dto.request.CustomerRequest;
import jakarta.validation.Valid;
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
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest customer) {
        return service.createCustomer(customer);
    }

    @GetMapping("/")
    public List<CustomerResponse> getAllCustomers(){
        return service.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(@PathVariable String customerId){
        return service.getCustomer(customerId);
    }

    @PutMapping("/{customerId}")
    public CustomerResponse updateCustomer(@PathVariable String customerId, @Valid @RequestBody CustomerRequest customer){
        return service.updateCustomer(customerId, customer);
    }

    @DeleteMapping("/{customerId}")
    public CustomerResponse deleteCustomer(@PathVariable String customerId){
        return service.deleteCustomer(customerId);
    }
}
