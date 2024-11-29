package dev.gunn96.popcat.exception;

public class GeoIpException extends RuntimeException {
    public GeoIpException(String message) {
        super(message);
    }

    public GeoIpException(String message, Throwable cause) {
        super(message, cause);
    }

    // GeoIP 데이터베이스 초기화 실패
    public static class DatabaseInitializationException extends GeoIpException {
        private static final String DEFAULT_MESSAGE = "Failed to initialize GeoIP database";

        public DatabaseInitializationException(Throwable cause) {
            super(DEFAULT_MESSAGE, cause);
        }

        public DatabaseInitializationException(String details, Throwable cause) {
            super(DEFAULT_MESSAGE + ": " + details, cause);
        }
    }

    // 잘못된 IP 형식
    public static class InvalidIpAddressException extends GeoIpException {
        private static final String DEFAULT_MESSAGE = "Invalid IP address format";

        public InvalidIpAddressException(Throwable cause) {
            super(DEFAULT_MESSAGE, cause);
        }

        public InvalidIpAddressException(String ipAddress, Throwable cause) {
            super(DEFAULT_MESSAGE + ": " + ipAddress, cause);
        }
    }

    // GeoIP 데이터베이스 조회 실패
    public static class DatabaseLookupException extends GeoIpException {
        private static final String DEFAULT_MESSAGE = "Failed to lookup IP address";

        public DatabaseLookupException(Throwable cause) {
            super(DEFAULT_MESSAGE, cause);
        }

        public DatabaseLookupException(String ipAddress, Throwable cause) {
            super(DEFAULT_MESSAGE + ": " + ipAddress, cause);
        }
    }
}