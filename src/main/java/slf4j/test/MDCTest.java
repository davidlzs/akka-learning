package slf4j.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

public class MDCTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MDCTest.class);
    public static final String X_REQUEST_ID = "x-request-id";

    public static void main(String[] args) {
        MDC.put(X_REQUEST_ID, UUID.randomUUID().toString());

        // do something

        LOGGER.info("x-request-id is: {}", MDC.get(X_REQUEST_ID));

        MDC.remove(X_REQUEST_ID);

        LOGGER.info("x-request-id is: {}", MDC.get(X_REQUEST_ID));

    }

}
