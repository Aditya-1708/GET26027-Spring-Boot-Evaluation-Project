package com.policy.api.constants;

public enum PolicyTerm {

    TERM_10(10),
    TERM_15(15),
    TERM_20(20),
    TERM_25(25),
    TERM_30(30);

    private final int years;

    PolicyTerm(int years) {
        this.years = years;
    }

    public int getYears() {
        return years;
    }
}