package com.zalphion.featurecontrol.lib.result;

import lombok.Data;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
@SuppressWarnings("unchecked")
public
class Failure<T> implements Result<T> {
    private final @NonNull @lombok.NonNull String message;

    @Override
    public <O> @NonNull @lombok.NonNull Result<O> flatMap(
            @NonNull @lombok.NonNull Function<T, Result<O>> function
    ) {
        return (Result<O>) this;
    }

    @Override
    public <O> @NonNull @lombok.NonNull Result<O> flatMapFailure(
            @NonNull @lombok.NonNull Function<String, Result<O>> function
    ) {
        return function.apply(message);
    }

    @Override
    public @NonNull @lombok.NonNull T recover(
            @NonNull @lombok.NonNull Function<String, T> function
    ) {
        return function.apply(message);
    }

    @Override
    public @NonNull @lombok.NonNull Result<T> peekFailure(
            @NonNull @lombok.NonNull Consumer<String> consumer
    ) {
        consumer.accept(message);
        return this;
    }

    @Override
    public @NonNull @lombok.NonNull <E extends Exception> T orElseThrow(
            @NonNull @lombok.NonNull Function<String, E> consumer
    ) throws E {
        throw consumer.apply(message);
    }

    @Override
    public @NonNull @lombok.NonNull Result<T> peek(@NonNull @lombok.NonNull Consumer<T> consumer) {
        return this;
    }
}
