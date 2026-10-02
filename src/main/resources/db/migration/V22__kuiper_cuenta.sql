-- Kuiper, cuentas y transferencias. Traidas de lumen-app.
-- Ver docs/decisiones/039-cuentas-y-transferencias.md.
--
-- Es V22 y no V20: se fusiono despues de V21 (notificaciones) y Flyway rechaza
-- una version menor que la ultima aplicada. V20 queda sin usar.
--
-- cuenta: nombre unico por usuario (con utf8mb4_unicode_ci, "ING" = "ing").
-- saldo_inicial DECIMAL(12,2) con signo: una tarjeta puede empezar en negativo.
-- El saldo actual NO se guarda: CuentaService lo calcula con SUM sobre
-- movimiento y transferencia, asi nunca se desincroniza.
--
-- movimiento.cuenta_id y recurrente.cuenta_id son opcionales: las filas que ya
-- existian se quedan a NULL ("sin cuenta") y no cuentan en ningun saldo.
--
-- transferencia: dinero entre dos cuentas del usuario. No es ingreso ni gasto
-- y el resumen mensual no la ve. Que las dos cuentas sean del usuario y
-- distintas lo comprueba TransferenciaService (el CHECK de abajo es solo la
-- red de seguridad de lo segundo).
--
-- Sin ON DELETE CASCADE: una cuenta con movimientos, recurrentes o
-- transferencias no se borra (se archiva), y la FK lo garantiza.

CREATE TABLE `cuenta` (
  `id`            bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id`    bigint        NOT NULL,
  `nombre`        varchar(100)  NOT NULL,
  `tipo`          enum('CORRIENTE','AHORRO','TARJETA','EFECTIVO') NOT NULL,
  `saldo_inicial` decimal(12,2) NOT NULL DEFAULT 0.00,
  `color`         varchar(7)    DEFAULT NULL,
  `icono`         varchar(50)   DEFAULT NULL,
  `banco`         varchar(100)  DEFAULT NULL,
  `archivada`     bit(1)        NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cuenta_usuario_nombre` (`usuario_id`, `nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `movimiento`
  ADD COLUMN `cuenta_id` bigint DEFAULT NULL,
  ADD KEY `idx_movimiento_cuenta` (`cuenta_id`),
  ADD CONSTRAINT `fk_movimiento_cuenta` FOREIGN KEY (`cuenta_id`) REFERENCES `cuenta` (`id`);

ALTER TABLE `recurrente`
  ADD COLUMN `cuenta_id` bigint DEFAULT NULL,
  ADD KEY `idx_recurrente_cuenta` (`cuenta_id`),
  ADD CONSTRAINT `fk_recurrente_cuenta` FOREIGN KEY (`cuenta_id`) REFERENCES `cuenta` (`id`);

CREATE TABLE `transferencia` (
  `id`                bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id`        bigint        NOT NULL,
  `cuenta_origen_id`  bigint        NOT NULL,
  `cuenta_destino_id` bigint        NOT NULL,
  `importe`           decimal(10,2) NOT NULL,
  `fecha`             date          NOT NULL,
  `concepto`          varchar(255)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_transferencia_usuario_fecha` (`usuario_id`, `fecha`),
  KEY `idx_transferencia_origen` (`cuenta_origen_id`),
  KEY `idx_transferencia_destino` (`cuenta_destino_id`),
  CONSTRAINT `fk_transferencia_origen` FOREIGN KEY (`cuenta_origen_id`) REFERENCES `cuenta` (`id`),
  CONSTRAINT `fk_transferencia_destino` FOREIGN KEY (`cuenta_destino_id`) REFERENCES `cuenta` (`id`),
  CONSTRAINT `chk_transferencia_cuentas_distintas` CHECK (`cuenta_origen_id` <> `cuenta_destino_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
