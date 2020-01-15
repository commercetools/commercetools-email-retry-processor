package com.commercetools.emailprocessor.utils;
import org.slf4j.MDC;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public final class CorrelationIdUtils {
    private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

    public static String getFromMDCOrGenerateNew() {
        return ofNullable(MDC.get(CORRELATION_ID_LOG_VAR_NAME))
            .orElseGet(CorrelationIdUtils::generateUniqueCorrelationIdAndAddToMDC);
    }

    private static String generateUniqueCorrelationIdAndAddToMDC() {
        final String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
        return correlationId;
    }

    private CorrelationIdUtils() {
    }
}
