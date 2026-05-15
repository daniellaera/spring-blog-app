package com.daniellaera.blog_app.utils;

import com.yubico.webauthn.data.ByteArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ByteArrayUtils {
    public static byte[] byteArrayToBytes(ByteArray byteArray) {
        return byteArray.getBytes();
    }

    public static ByteArray bytesToByteArray(byte[] bytes) {
        return new ByteArray(bytes);
    }
}
