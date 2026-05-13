package com.anils.library.service;

import java.util.Objects;

public class FixedFeeCalculator implements FeeCalculator {

    private final double fee;

    public FixedFeeCalculator(double fee) {
        if (fee < 0) {
            throw new IllegalArgumentException("fee must be non-negative");
        }
        this.fee = fee;
    }

    @Override
    public double borrowFeeAmount() {
        return fee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FixedFeeCalculator that = (FixedFeeCalculator) o;
        return Double.compare(that.fee, fee) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fee);
    }
}
