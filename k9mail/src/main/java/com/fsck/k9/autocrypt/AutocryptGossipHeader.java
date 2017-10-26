package com.fsck.k9.autocrypt;


import java.util.Arrays;

import android.support.annotation.NonNull;

import okio.ByteString;


class AutocryptGossipHeader {
    static final String AUTOCRYPT_GOSSIP_HEADER = "Autocrypt-Gossip";

    private static final String AUTOCRYPT_PARAM_ADDR = "addr";
    private static final String AUTOCRYPT_PARAM_KEY_DATA = "keydata";

    private static final int HEADER_LINE_LENGTH = 76;


    @NonNull
    private final byte[] keyData;
    @NonNull
    private final String addr;

    AutocryptGossipHeader(@NonNull String addr, @NonNull byte[] keyData) {
        this.addr = addr;
        this.keyData = keyData;
    }

    String toRawHeaderString() {
        StringBuilder builder = new StringBuilder();
        builder.append(AutocryptGossipHeader.AUTOCRYPT_GOSSIP_HEADER).append(": ");
        builder.append(AutocryptGossipHeader.AUTOCRYPT_PARAM_ADDR).append('=').append(addr).append("; ");
        builder.append(AutocryptGossipHeader.AUTOCRYPT_PARAM_KEY_DATA).append("=");

        appendBase64KeyData(builder);

        return builder.toString();
    }

    private void appendBase64KeyData(StringBuilder builder) {
        String base64KeyData = ByteString.of(keyData).base64();

        int base64Length = base64KeyData.length();
        int lineLengthBeforeKeyData = builder.length();
        int dataLengthInFirstLine = HEADER_LINE_LENGTH -lineLengthBeforeKeyData;

        boolean keyDataFitsInFirstLine = dataLengthInFirstLine > 0 && base64Length < dataLengthInFirstLine;
        if (keyDataFitsInFirstLine) {
            builder.append(base64KeyData, 0, base64Length);
            return;
        }

        if (dataLengthInFirstLine > 0) {
            builder.append(base64KeyData, 0, dataLengthInFirstLine).append("\r\n ");
        } else {
            builder.append("\r\n ");
            dataLengthInFirstLine = 0;
        }

        for (int i = dataLengthInFirstLine; i < base64Length; i += HEADER_LINE_LENGTH) {
            if (i + HEADER_LINE_LENGTH <= base64Length) {
                builder.append(base64KeyData, i, i + HEADER_LINE_LENGTH).append("\r\n ");
            } else {
                builder.append(base64KeyData, i, base64Length);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AutocryptGossipHeader that = (AutocryptGossipHeader) o;

        if (!Arrays.equals(keyData, that.keyData)) {
            return false;
        }
        if (!addr.equals(that.addr)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(keyData);
        result = 31 * result + addr.hashCode();
        return result;
    }
}
