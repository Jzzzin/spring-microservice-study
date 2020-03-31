package com.bloknoma.licenses.hystrix;

import com.bloknoma.licenses.utils.UserContext;
import com.bloknoma.licenses.utils.UserContextHolder;

import java.util.concurrent.Callable;

public final class DelegatingUserContextCallable<V> implements Callable<V> {
    private final Callable<V> delegate;
    private UserContext originalUserContext;

    public DelegatingUserContextCallable(Callable<V> delegate, UserContext originalUserContext) {
        this.delegate = delegate;
        this.originalUserContext = originalUserContext;
    }

    public DelegatingUserContextCallable(Callable<V> delegate) {
        this(delegate, UserContextHolder.getContext());
    }

    public V call() throws Exception {
        UserContextHolder.setContext(originalUserContext);

        try {
            return delegate.call();
        } finally {
            this.originalUserContext = null;
        }
    }

    public String toString() {
        return delegate.toString();
    }

    public static <V> Callable<V> create(Callable<V> delegate, UserContext userContext) {
        return new DelegatingUserContextCallable<V>(delegate, userContext);
    }
}
