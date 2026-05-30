package com.zalphion.featurecontrol.lib;

import lombok.val;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BinaryUtils {

    private static final int CHUNK_SIZE = 8192;

    public static byte[] readFully(@NonNull @lombok.NonNull InputStream inputStream) throws IOException {
        try (val outputStream = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[CHUNK_SIZE];

            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) != -1) {
                outputStream.write(chunk, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }
}
