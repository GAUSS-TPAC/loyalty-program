package com.yowyob.loyalty.shared.exception;

public class DeveloperInviteException extends AppException {
    public DeveloperInviteException(String detail) {
        super(ErrorCode.DEVELOPER_INVITE_FAILED, detail);
    }
}
