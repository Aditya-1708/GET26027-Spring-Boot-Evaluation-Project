package com.policy.api.service;

import com.policy.api.dto.request.CustomerRequest;
import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.exception.CustomerNotFoundException;
import com.policy.api.exception.InvalidCustomerException;
import com.policy.api.model.Customer;
import com.policy.api.repository.CustomerRepository;
import com.policy.api.util.IdGenerator;
import com.policy.api.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private IdGenerator generator;

    @Mock
    private Validation validation;

    @InjectMocks
    private CustomerService service;

    @Test
    void shouldCreateCustomerSuccessfully() {

        CustomerRequest request = new CustomerRequest(
                "Aditya",
                "Umesh",
                22,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore"
        );

        when(validation.validateCustomer(request)).thenReturn("true");
        when(generator.generateCustomerId()).thenReturn("CUST001");

        when(repository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = service.createCustomer(request);

        assertNotNull(response);
        assertEquals("CUST001", response.getCustomerId());
        assertEquals("Aditya", response.getFirstName());

        verify(repository, times(1)).save(any(Customer.class));
    }

    @Test
    void shouldThrowInvalidCustomerExceptionWhenValidationFails() {

        CustomerRequest request = new CustomerRequest(
                "Aditya",
                "Umesh",
                15,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore"
        );

        when(validation.validateCustomer(request))
                .thenReturn("Customer age must be between 18 and 65 years.");

        assertThrows(
                InvalidCustomerException.class,
                () -> service.createCustomer(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnCustomerById() {

        Customer customer = new Customer(
                "CUST001",
                "Aditya",
                "Umesh",
                22,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore"
        );

        when(repository.get("CUST001")).thenReturn(customer);

        CustomerResponse response = service.getCustomer("CUST001");

        assertNotNull(response);
        assertEquals("CUST001", response.getCustomerId());
        assertEquals("Aditya", response.getFirstName());
    }

    @Test
    void shouldThrowCustomerNotFoundException() {

        when(repository.get("CUST999"))
                .thenReturn(null);

        assertThrows(
                CustomerNotFoundException.class,
                () -> service.getCustomer("CUST999")
        );
    }

    @Test
    void shouldReturnAllCustomers() {

        List<Customer> customers = List.of(
                new Customer(
                        "CUST001",
                        "Aditya",
                        "Umesh",
                        22,
                        "Male",
                        "9876543210",
                        "aditya@gmail.com",
                        "Bangalore"
                ),
                new Customer(
                        "CUST002",
                        "Rahul",
                        "Sharma",
                        25,
                        "Male",
                        "9999999999",
                        "rahul@gmail.com",
                        "Mumbai"
                )
        );

        when(repository.get()).thenReturn(customers);

        List<CustomerResponse> response =
                service.getAllCustomers();

        assertEquals(2, response.size());
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {

        Customer existingCustomer = new Customer(
                "CUST001",
                "Aditya",
                "Umesh",
                22,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore"
        );

        CustomerRequest request = new CustomerRequest(
                "Aditya",
                "Umesh",
                23,
                "Male",
                "9999999999",
                "aditya.updated@gmail.com",
                "Mumbai"
        );

        when(repository.get("CUST001"))
                .thenReturn(existingCustomer);

        when(validation.validateCustomer(request))
                .thenReturn("true");

        when(repository.update(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response =
                service.updateCustomer("CUST001", request);

        assertEquals(23, response.getAge());
        assertEquals("Mumbai", response.getAddress());

        verify(repository).update(any(Customer.class));
    }

    @Test
    void shouldThrowCustomerNotFoundWhenUpdating() {

        CustomerRequest request = new CustomerRequest(
                "Aditya",
                "Umesh",
                23,
                "Male",
                "9999999999",
                "aditya@gmail.com",
                "Mumbai"
        );

        when(repository.get("CUST999"))
                .thenReturn(null);

        assertThrows(
                CustomerNotFoundException.class,
                () -> service.updateCustomer("CUST999", request)
        );
    }
}