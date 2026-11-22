# Banco Académico UDB – Sistema de Gestión Bancaria (Fase 1)

Proyecto de cátedra de POO901, Universidad Don Bosco, Grupo 7. Aplicación de escritorio en Java (Swing) que registra clientes y cuentas bancarias en MySQL mediante JDBC.

## Estructura

```
database/banco_academico_udb.sql   script de la base de datos
lib/mysql-connector-j-9.7.0.jar    conector de MySQL
src/db.properties                  datos de conexión
src/vista/MenuPrincipal.java       clase que se ejecuta
src/{conexion,modelo,dao,validacion,vista,pruebas}/
docs/pruebas/                      casos de prueba, registro de problemas y evidencias
docs/defensa/                      guion de la demostración
```

## Cómo ejecutarlo

Requisitos: JDK 21, MySQL Server 8.4 y MySQL Workbench. El conector ya está en `lib/`.

1. En MySQL Workbench, conectado como root, ejecutar `database/banco_academico_udb.sql`. Crea la base, las tablas, los datos de prueba y el usuario `udb_general` que usa el programa.
2. Abrir la carpeta en IntelliJ IDEA. Si pide el JDK, elegir el 21 en File > Project Structure > Project.
3. Ejecutar `src/vista/MenuPrincipal.java` (clic derecho > Run).

`src/db.properties` ya trae los datos del usuario que crea el script; solo se cambia si MySQL usa otro puerto u otro usuario. Para dejar la base solo con los datos de prueba: `DROP DATABASE banco_academico_udb;` y volver a ejecutar el script.

| Error | Solución |
|---|---|
| No se encontró db.properties | Debe estar en `src/`; luego Build > Rebuild Project |
| No suitable driver found | Agregar `lib/mysql-connector-j-9.7.0.jar` en Project Structure > Libraries |
| Access denied / Unknown database | Ejecutar el script SQL |
| Communications link failure | Encender el servicio de MySQL |

## Reglas del sistema

- Código de cliente, DUI y número de cuenta no se repiten.
- Todo cliente nuevo queda ACTIVO. Toda cuenta nueva queda ACTIVA, con saldo 0.00 y la fecha del día.
- Modificar no cambia el estado del cliente; para eso está Cambiar estado.
- No se borran clientes ni cuentas.
- Solo se crean cuentas para clientes ACTIVO.

## Pruebas

31 casos de prueba, ejecutados el 2026-11-08: 31 aprobados. Ver [casos de prueba](docs/pruebas/casos_de_prueba.md), [registro de problemas](docs/pruebas/registro_de_problemas.md) y [evidencias](docs/pruebas/evidencias/).
