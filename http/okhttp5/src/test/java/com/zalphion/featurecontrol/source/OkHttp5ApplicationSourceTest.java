package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.http.HttpFunction;
import com.zalphion.featurecontrol.http.OkHttp3HttpFunction;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.time.Instant;
import java.util.function.Supplier;

public class OkHttp5ApplicationSourceTest extends ApplicationSourceContract {

    @Override
    protected @NonNull HttpFunction createHttpFunction(@NonNull Supplier<Instant> clock) throws IOException {
        return new OkHttp3HttpFunction();
    }
}
