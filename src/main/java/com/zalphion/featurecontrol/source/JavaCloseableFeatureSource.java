package com.zalphion.featurecontrol.source;

import org.jetbrains.annotations.NotNull;

public class JavaCloseableFeatureSource extends JavaFeatureSource implements AutoCloseable {

    private @NotNull final AutoCloseable closeable;

    public JavaCloseableFeatureSource(@NotNull CloseableFeatureSource source) {
        super(source);
        this.closeable = source;
    }

    @Override
    public void close() throws Exception {
        closeable.close();
    }
}
