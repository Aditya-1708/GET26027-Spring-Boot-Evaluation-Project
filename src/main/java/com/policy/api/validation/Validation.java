package com.policy.api.validation;

import com.policy.api.dto.request.CustomerRequest;
import com.policy.api.dto.request.ProposalRequest;
import com.policy.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Validation {


    public String validateCustomer(CustomerRequest customer){
        if(customer.getAge() < 18 || customer.getAge() > 65){
            return "Customer age must be between 18 and 65 years.";
        }
        return "true";
    }

    public String validateProposal(ProposalRequest proposal){
        final int term = proposal.getPolicyTerm();
        final int sumAssured = proposal.getSumAssured();
        final int annualPremium = proposal.getPremium();
        final String PAN = proposal.getPAN();

        if(term != 10 && term != 15 && term != 20 && term != 25 && term != 30){
            return "Invalid policy term";
        }
        else if(sumAssured < 100000 || sumAssured > 50000000){
            return "Assured Sum is not in the recommended range";
        }
        else if(annualPremium < 5000){
            return "premium less than minimum requirement";
        }
        else if(annualPremium > 50000 && !(PAN == null || PAN.isBlank() || PAN.matches("^[A-Z]{5}\\d{4}[A-Z]$")) ){
            return "PAN number is mandatory for policies with annual premium of 50000";
        }
        else{
            return "true";
        }

    }
}
