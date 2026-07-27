package com.policy.api.service;

import com.policy.api.dto.request.CustomerRequest;
import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.exception.CustomerNotFoundException;
import com.policy.api.exception.InvalidCustomerException;
import com.policy.api.model.Customer;
import com.policy.api.model.Proposal;
import com.policy.api.repository.CustomerRepository;
import com.policy.api.repository.ProposalRepository;
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

    private final ProposalRepository proposalRepository;

    private final IdGenerator generator;

    private final Validation validation;

    private final MaskPii maskPii;

    public CustomerService(
            CustomerRepository repository,
            IdGenerator generator,
            Validation validation,
            ProposalRepository proposalRepository,
            MaskPii maskPii) {

        this.repository = repository;
        this.generator = generator;
        this.validation = validation;
        this.proposalRepository = proposalRepository;
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

    public boolean hasActiveProposals(String customerId) {

        List<Proposal> fetchedProposals = proposalRepository.getByCustomerId(customerId);

        if (fetchedProposals.isEmpty()) {
            return false;
        }

        return !fetchedProposals.stream()
                .allMatch(Proposal::isDeleted);
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

        Customer updatedCustomer = new Customer(
                existingCustomer.getCustomerId(),
                customerRequest.getFirstName(),
                customerRequest.getLastName(),
                customerRequest.getAge(),
                customerRequest.getGender(),
                customerRequest.getMobileNumber(),
                customerRequest.getEmail(),
                customerRequest.getAddress(),
                existingCustomer.isDeleted(),
                existingCustomer.getDeletedAt()
        );

        Customer savedCustomer = repository.save(updatedCustomer);

        return mapToResponse(savedCustomer);
    }


    public CustomerResponse deleteCustomer(String customerId) {
        Customer existingCustomer = getActiveCustomer(customerId);

        if (hasActiveProposals(customerId)) {
            throw new InvalidCustomerException("Customer has active proposals and cannot be deleted.");
        }

        Customer deletedCustomer = new Customer(
                existingCustomer.getCustomerId(),
                existingCustomer.getFirstName(),
                existingCustomer.getLastName(),
                existingCustomer.getAge(),
                existingCustomer.getGender(),
                existingCustomer.getMobileNumber(),
                existingCustomer.getEmail(),
                existingCustomer.getAddress(),
                true,
                LocalDateTime.now()
        );

        Customer savedCustomer = repository.save(deletedCustomer);

        return mapToResponse(savedCustomer);
    }
}
