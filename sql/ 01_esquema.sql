-- DistriSalva - esquema de la Guia 5. Ejecutar en Supabase > SQL Editor.
CREATE TABLE IF NOT EXISTS producto (
                                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        codigo VARCHAR(10) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    categoria VARCHAR(40) NOT NULL,
    precio NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    existencia INTEGER NOT NULL CHECK (existencia >= 0),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ
    );
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto (categoria);
-- Cierra la puerta de la API REST publica: sin politicas, nadie con la
-- clave publica puede leer ni escribir. JDBC entra como duenio de la tabla.
ALTER TABLE producto ENABLE ROW LEVEL SECURITY;
