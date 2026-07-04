package com.yowyob.loyalty.shared.exception;

public class KernelCoreUnavailableException extends AppException {
    public KernelCoreUnavailableException(String detail) {
        super(ErrorCode.KERNEL_CORE_UNAVAILABLE, detail);
    }
}
