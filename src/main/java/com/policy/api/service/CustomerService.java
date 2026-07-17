package com.policy.api.service;

import com.policy.api.dto.request.CustomerRequest;
import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.exception.CustomerNotFoundException;
import com.policy.api.exception.InvalidCustomerException;
import com.policy.api.model.Customer;
import com.policy.api.repository.CustomerRepository;
import com.policy.api.util.IdGenerator;
import com.policy.api.util.MaskPii;
import com.policy.api.validation.Validation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class CustomerService {

    private final CustomerRepository repository;

    private final IdGenerator generator;

    private final Validation validation;

    private final MaskPii maskPii;

    public CustomerService(
            CustomerRepository repository,
            IdGenerator generator,
            Validation validation,
            MaskPii maskPii) {

        this.repository = repository;
        this.generator = generator;
        this.validation = validation;
        this.maskPii = maskPii;
    }


    private Customer getActiveCustomer(String customerId) {
        Customer customer = repository.get(customerId);

        if (customer == null || customer.isDeleted()) {
            throw new CustomerNotFoundException(customerId);
        }

        return customer;
    }


    private Customer mapToModel(CustomerRequest customer) {
        return new Customer(generator.generateCustomerId(), customer.getFirstName(), customer.getLastName(), customer.getAge(), customer.getGender(), customer.getMobileNumber(), customer.getEmail(), customer.getAddress(), false, null);
    }


    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getAge(),
                customer.getGender(),
                maskPii.maskMobile(customer.getMobileNumber()),
                maskPii.maskEmail(customer.getEmail()),
                customer.getAddress()
        );
    }


    public CustomerResponse createCustomer(CustomerRequest customer) {
        String isValid = validation.validateCustomer(customer);
        if (!isValid.equals("true")) {
            throw new InvalidCustomerException(isValid);
        } else {

            Customer newCustomer = mapToModel(customer);
            Customer savedCustomer = repository.save(newCustomer);

            return mapToResponse(savedCustomer);
        }
    }


    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customers = repository.get();
        List<CustomerResponse> fetchedCustomers = new ArrayList<>();

        for (Customer customer : customers) {
            if (!customer.isDeleted()) {
                fetchedCustomers.add(mapToResponse(customer));
            }
        }

        return fetchedCustomers;
    }


    public CustomerResponse getCustomer(String customerId) {

        return mapToResponse(getActiveCustomer(customerId));

    }


    public CustomerResponse updateCustomer(String customerId, CustomerRequest customerRequest) {


        Customer existingCustomer = getActiveCustomer(customerId);

        String isValid = validation.validateCustomer(customerRequest);

        if (!isValid.equals("true")) {
            throw new InvalidCustomerException(isValid);
        }

        existingCustomer.setFirstName(customerRequest.getFirstName());
        existingCustomer.setLastName(customerRequest.getLastName());
        existingCustomer.setAge(customerRequest.getAge());
        existingCustomer.setGender(customerRequest.getGender());
        existingCustomer.setMobileNumber(customerRequest.getMobileNumber());
        existingCustomer.setEmail(customerRequest.getEmail());
        existingCustomer.setAddress(customerRequest.getAddress());


        return mapToResponse(existingCustomer);
    }


    public CustomerResponse deleteCustomer(String customerId) {
        Customer existingCustomer = getActiveCustomer(customerId);

        existingCustomer.setDeleted(true);
        existingCustomer.setDeletedAt(LocalDateTime.now());

        return mapToResponse(existingCustomer);
    }
}
