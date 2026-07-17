package com.yowyob.loyalty.sdk;

/**
 * La signature du callback webhook est absente, invalide ou expirée.
 * Ne traitez JAMAIS un callback qui lève cette exception.
 */
public class SignatureVerificationException extends LoyaltyException {
    public SignatureVerificationException(String message) {
        super(message);
    }
}
