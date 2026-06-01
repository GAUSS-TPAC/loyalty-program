package com.yowyob.loyaulty.program.shared.exception;

public class WalletNotFoundException extends AppException {
    public WalletNotFoundException(String memberId) {
        super(ErrorCode.WALLET_NOT_FOUND, "Wallet not found for member: " + memberId);
    }
}
