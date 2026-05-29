package com.zalphion.featurecontrol.lib.result;

import lombok.Data;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
@SuppressWarnings("unchecked")
public
class Success<T> implements Result<T> {
    private final @NonNull @lombok.NonNull T value;

    @Override
    public <O> @NonNull @lombok.NonNull Result<O> flatMap(
            @NonNull @lombok.NonNull Function<T, Result<O>> function
    ) {
        return function.apply(value);
    }

    @Override
    public <O> @NonNull @lombok.NonNull Result<O> flatMapFailure(
            @NonNull @lombok.NonNull Function<String, Result<O>> function
    ) {
        return (Result<O>) this;
    }

    @Override
    public @NonNull @lombok.NonNull T recover(
            @NonNull @lombok.NonNull Function<String, T> function
    ) {
        return value;
    }

    @Override
    public @NonNull @lombok.NonNull Result<T> peekFailure(
            @NonNull @lombok.NonNull Consumer<String> consumer
    ) {
        return this;
    }

    @Override
    public @NonNull @lombok.NonNull <E extends Exception> T orElseThrow(
            @NonNull @lombok.NonNull Function<String, E> consumer
    ) {
        return value;
    }

    @Override
    public @NonNull @lombok.NonNull Result<T> peek(@NonNull @lombok.NonNull Consumer<T> consumer) {
        consumer.accept(value);
        return this;
    }
}
