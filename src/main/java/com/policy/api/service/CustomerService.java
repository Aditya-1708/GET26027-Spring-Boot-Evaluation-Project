package com.policy.api.service;

import com.policy.api.dto.request.CustomerRequest;
import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.exception.InvalidCustomerException;
import com.policy.api.model.Customer;
import com.policy.api.repository.CustomerRepository;
import com.policy.api.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class CustomerService {
    private final CustomerRepository repository;

    private final IdGenerator generator;

    public CustomerService(CustomerRepository repository, IdGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    public CustomerResponse createCustomer(CustomerRequest customer) {
        if (customer.getAge() < 18 || customer.getAge() > 65) {
            throw new InvalidCustomerException("Customer age must be between 18 and 65 years.");
        } else {
            Customer newCustomer = new Customer(generator.generateCustomerID(), customer.getFirstName(), customer.getLastName(), customer.getAge(), customer.getGender(), customer.getMobileNumber(), customer.getEmail(), customer.getAddress());

            Customer savedCustomer = repository.save(newCustomer);

            return new CustomerResponse(savedCustomer.getCustomerId(), savedCustomer.getFirstName(), savedCustomer.getLastName(), savedCustomer.getAge(), savedCustomer.getGender(), savedCustomer.getMobileNumber(), savedCustomer.getEmail(), savedCustomer.getAddress());
        }
    }

    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customers = repository.get();
        List<CustomerResponse> fetchedCustomers = new ArrayList<>();
        for (Customer c : customers) {
            CustomerResponse temp = new CustomerResponse(c.getCustomerId(), c.getFirstName(), c.getLastName(), c.getAge(), c.getGender(), c.getMobileNumber(), c.getEmail(), c.getAddress());
            fetchedCustomers.add(temp);
        }
        return fetchedCustomers;
    }

    public CustomerResponse getCustomer(String customerId) {
        Customer fetchedCustomer = repository.get(customerId);

        return new CustomerResponse(fetchedCustomer.getCustomerId(), fetchedCustomer.getFirstName(), fetchedCustomer.getLastName(), fetchedCustomer.getAge(), fetchedCustomer.getGender(), fetchedCustomer.getMobileNumber(), fetchedCustomer.getEmail(), fetchedCustomer.getAddress());
    }

    public CustomerResponse updateCustomer(String customerId, CustomerRequest customerRequest) {

        // Fetch existing customer
        Customer existingCustomer = repository.get(customerId);

        // Customer not found
        if (existingCustomer == null) {
            throw new InvalidCustomerException("Customer with ID " + customerId + " not found.");
        }

        // Business validation
        if (customerRequest.getAge() < 18 || customerRequest.getAge() > 65) {
            throw new InvalidCustomerException("Customer age must be between 18 and 65 years.");
        }

        // Update existing object
        existingCustomer.setFirstName(customerRequest.getFirstName());
        existingCustomer.setLastName(customerRequest.getLastName());
        existingCustomer.setAge(customerRequest.getAge());
        existingCustomer.setGender(customerRequest.getGender());
        existingCustomer.setMobileNumber(customerRequest.getMobileNumber());
        existingCustomer.setEmail(customerRequest.getEmail());
        existingCustomer.setAddress(customerRequest.getAddress());

        // Save updated customer
        Customer updatedCustomer = repository.update(existingCustomer);

        // Convert Model -> Response DTO
        return new CustomerResponse(updatedCustomer.getCustomerId(), updatedCustomer.getFirstName(), updatedCustomer.getLastName(), updatedCustomer.getAge(), updatedCustomer.getGender(), updatedCustomer.getMobileNumber(), updatedCustomer.getEmail(), updatedCustomer.getAddress());
    }
}
