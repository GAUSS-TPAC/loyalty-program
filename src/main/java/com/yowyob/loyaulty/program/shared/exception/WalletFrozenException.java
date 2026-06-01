package com.yowyob.loyaulty.program.shared.exception;

public class WalletFrozenException extends AppException {
    public WalletFrozenException(String walletId) {
        super(ErrorCode.WALLET_FROZEN, "Wallet is frozen: " + walletId);
    }
}
