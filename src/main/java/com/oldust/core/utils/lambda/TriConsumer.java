package com.oldust.core.utils.lambda;

import com.google.common.base.Preconditions;

@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> then) {
        Preconditions.checkNotNull(then);

        return (a, b, c) -> {
            accept(a, b, c);
            then.accept(a, b, c);
        };

    }

}