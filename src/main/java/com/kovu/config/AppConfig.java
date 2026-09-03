package com.kovu.config;

/**
 * Configuración de la app leída de variables de entorno (12-factor), con
 * valores por defecto pensados para correr todo local sin configurar nada
 * (ver docker-compose.yml). En producción, estas variables las inyecta el
 * orquestador (Docker, Kubernetes, etc.), nunca se hardcodean.
 */
public record AppConfig(
        int httpPort,
        String dbUrl,
        String dbUser,
        String dbPassword,
        int dbPoolSize
) {

    public static AppConfig fromEnv() {
        return new AppConfig(
                intEnv("APP_PORT", 8080),
                env("DB_URL", "jdbc:mysql://localhost:3306/kovu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"),
                env("DB_USER", "kovu"),
                env("DB_PASSWORD", "kovu"),
                intEnv("DB_POOL_SIZE", 8)
        );
    }

    private static String env(String key, String valorPorDefecto) {
        String valor = System.getenv(key);
        return (valor == null || valor.isBlank()) ? valorPorDefecto : valor;
    }

    private static int intEnv(String key, int valorPorDefecto) {
        String valor = System.getenv(key);
        return (valor == null || valor.isBlank()) ? valorPorDefecto : Integer.parseInt(valor);
    }
}
