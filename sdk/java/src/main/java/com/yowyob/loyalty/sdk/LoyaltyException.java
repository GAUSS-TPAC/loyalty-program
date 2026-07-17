package com.yowyob.loyalty.sdk;

/** Exception de base du SDK Yowyob Loyalty. */
public class LoyaltyException extends RuntimeException {
    public LoyaltyException(String message) {
        super(message);
    }

    public LoyaltyException(String message, Throwable cause) {
        super(message, cause);
    }
}
