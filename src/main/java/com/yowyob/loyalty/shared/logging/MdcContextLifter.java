package com.yowyob.loyalty.shared.logging;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper that copies the state of Reactor [Context] to MDC on the onNext function.
 */
public class MdcContextLifter<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> coreSubscriber;

    public MdcContextLifter(CoreSubscriber<T> coreSubscriber) {
        this.coreSubscriber = coreSubscriber;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        coreSubscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(T t) {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onNext(t);
    }

    @Override
    public void onError(Throwable throwable) {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onComplete();
    }

    @Override
    public Context currentContext() {
        return coreSubscriber.currentContext();
    }

    private void copyToMdc(Context context) {
        if (!context.isEmpty()) {
            Map<String, String> map = context.stream()
                    .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                    .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
            MDC.setContextMap(map);
        } else {
            MDC.clear();
        }
    }
}
