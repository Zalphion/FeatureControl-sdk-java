package com.zalphion.featurecontrol.http;

import com.zalphion.featurecontrol.source.ApplicationSourceContract;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.time.Instant;
import java.util.function.Supplier;

public class OkHttp4ApplicationSourceTest extends ApplicationSourceContract {

    @Override
    protected @NonNull HttpFunction createHttpFunction(@NonNull Supplier<Instant> clock) throws IOException {
        return new OkHttp3HttpFunction();
    }
}
