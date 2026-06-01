package com.yowyob.loyaulty.program.api.wallet.dto.response;

import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;

import java.util.List;

public record TransactionPageResponse(
        List<TransactionResponse> content,
        long total,
        int page,
        int totalPages
) {
    public static TransactionPageResponse from(PageResult<WalletTransaction> page) {
        return new TransactionPageResponse(
                page.content().stream().map(TransactionResponse::from).toList(),
                page.totalElements(),
                page.page(),
                page.totalPages()
        );
    }
}
