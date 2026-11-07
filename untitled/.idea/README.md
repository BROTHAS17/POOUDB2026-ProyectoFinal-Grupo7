# Proyecto Banco Académico UDB - Módulo Integrado Clientes (Integrante 2 + Persona 1)

## Estructura Integrada
```
Modulo_Clientes_UDB_Integrado/
├── db.properties
├── README.md
├── sql/
│   └── banco_academico_udb.sql
└── src/
    ├── conexion/
    │   └── Conexion.java
    ├── dao/
    │   └── ClienteDAO.java
    ├── modelo/
    │   └── Cliente.java
    ├── validacion/
    │   └── ValidadorCliente.java
    ├── vista/
    │   └── VentanaClientes.java
    └── pruebas/
        └── PruebasIntegracion.java
```

## Datos Iniciales en BD (Persona 1)
- **Cliente 1**: `CLI001` | DUI: `12345678-9` | Ana Lopez | Tel: `7000-0001` | `ana.lopez@example.com`
- **Cliente 2**: `CLI002` | DUI: `98765432-1` | Carlos Martinez | Tel: `7000-0002` | `carlos.martinez@example.com`

## Pasos para Integrar y Probar
1. Importar el archivo `../../sql/banco_academico_udb.sql` en MySQL Workbench.
2. Copiar el paquete `../../src` a tu proyecto de IntelliJ IDEA.
3. Asegurar que `mysql-connector-j-9.7.0.jar` esté agregado en *Project Structure > Libraries*.
4. Configurar tus credenciales de MySQL en `../../db.properties`.
5. Ejecutar `vista.VentanaClientes` para visualizar y manipular automáticamente los registros creados por la Persona 1.
