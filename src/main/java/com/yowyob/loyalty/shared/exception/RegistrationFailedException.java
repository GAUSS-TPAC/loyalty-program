package com.yowyob.loyalty.shared.exception;

public class RegistrationFailedException extends AppException {
    public RegistrationFailedException(String detail) {
        super(ErrorCode.REGISTRATION_FAILED, detail);
    }
}
