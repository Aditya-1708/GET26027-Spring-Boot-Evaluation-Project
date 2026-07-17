package com.policy.api.repository;
import com.policy.api.dto.request.CustomerRequest;
import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Repository
public class CustomerRepository {

    private final Map<String, Customer> map;

    public CustomerRepository(){
        this.map = new ConcurrentHashMap<>();
    }


    public Customer save(Customer customer){
        map.put(customer.getCustomerId(), customer);
        return customer;
    }

    public List<Customer>  get(){
        List<Customer> customers = new ArrayList<>(map.values());
        return customers;
    }

    public Customer get(String customerId){
        return map.getOrDefault(customerId,null);
    }


}
