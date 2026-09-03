-- V1: tabla base de productos.
-- InnoDB explícito (soporta transacciones e integridad referencial real,
-- a diferencia de MyISAM). uq_productos_sku es un índice único real: el
-- INSERT duplicado falla en la base, no confiamos solo en el chequeo
-- previo del service (esa carrera -dos inserts casi simultáneos con el
-- mismo sku- es justamente lo que el índice único evita).
CREATE TABLE IF NOT EXISTS productos (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku              VARCHAR(64)   NOT NULL,
    nombre           VARCHAR(200)  NOT NULL,
    precio_centavos  BIGINT        NOT NULL,
    stock            INT           NOT NULL DEFAULT 0,
    activo           BOOLEAN       NOT NULL DEFAULT TRUE,
    creado_en        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_productos_sku UNIQUE (sku)
) ENGINE=InnoDB;
