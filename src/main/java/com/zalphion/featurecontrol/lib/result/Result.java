package com.zalphion.featurecontrol.lib.result;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T> {

    <O> @NonNull @lombok.NonNull Result<O> flatMap(
            @NonNull @lombok.NonNull Function<T, Result<O>> function
    );

    <O> @NonNull @lombok.NonNull Result<O> flatMapFailure(
            @NonNull @lombok.NonNull Function<String, Result<O>> function
    );

    @NonNull @lombok.NonNull T recover(
            @NonNull @lombok.NonNull Function<String, T> function
    );

    @NonNull @lombok.NonNull Result<T> peek(
            @NonNull @lombok.NonNull Consumer<T> consumer
    );

    @NonNull @lombok.NonNull Result<T> peekFailure(
            @NonNull @lombok.NonNull Consumer<String> consumer
    );

    @NonNull @lombok.NonNull <E extends Exception> T orElseThrow(
            @NonNull @lombok.NonNull Function<String, E> consumer
    ) throws E;

    default @NonNull @lombok.NonNull <O> Result<O> map(
            @NonNull @lombok.NonNull Function<T, O> function
    ) {
        return flatMap( value -> new Success<>(function.apply(value)));
    }

    default @NonNull @lombok.NonNull Result<T> mapFailure(
            @NonNull @lombok.NonNull Function<String, String> function
    ) {
        return flatMapFailure(message -> new Failure<>(function.apply(message)));
    }

    static @NonNull @lombok.NonNull <T> Result<T> successOr(
            T value,
            @NonNull @lombok.NonNull Supplier<String> errorFunction
    ) {
        return value == null ? new Failure<>(errorFunction.get()) : new Success<>(value);
    }
}