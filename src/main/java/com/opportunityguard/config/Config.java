package com.opportunityguard.config;

/**
 * Reads configuration from environment variables first, then JVM system properties (-DNAME=value).
 * No secrets are stored in source code.
 */
public final class Config {
    private Config() {}

    public static String get(String key, String defaultValue) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v.trim();
    }

    public static boolean isSet(String key) {
        return !get(key, "").isEmpty();
    }

    public static String dbUrl() {
        return get("DB_URL", "jdbc:mysql://localhost:3306/opportunityguard"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
    }

    public static String dbUser() { return get("DB_USER", "root"); }

    public static String dbPassword() { return get("DB_PASSWORD", ""); }
}
