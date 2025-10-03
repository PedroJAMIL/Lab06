-- Script SQL para limpiar la tabla intenciones
-- Eliminar la columna fecha duplicada, mantener solo fecha_creacion
ALTER TABLE intenciones DROP COLUMN fecha;