# Guion de la demostración en vivo (defensa, parte práctica)

Duración estimada: 7 a 8 minutos. Los casos de prueba (CP-XX) están en [../pruebas/casos_de_prueba.md](../pruebas/casos_de_prueba.md). Todos los pasos de este guion se probaron el 2026-11-08 y se aprobaron.

**[POR DEFINIR: quién presenta cada parte.]**

## Antes de empezar (preparar 15 minutos antes)

- [ ] Dejar la base solo con los datos de prueba (Ana Lopez, Carlos Martinez y CTA001): en Workbench ejecutar `DROP DATABASE banco_academico_udb;` y luego el script `database/banco_academico_udb.sql`.
- [ ] Hacer Build > Rebuild Project, ejecutar `vista.MenuPrincipal`, abrir clientes una vez para comprobar que conecta y cerrar el programa.
- [ ] Dejar abierto MySQL Workbench con una pestaña que tenga estas consultas:
  ```sql
  SELECT * FROM clientes;
  SELECT c.numero_cuenta, c.tipo_cuenta, c.saldo, c.estado, cl.nombre_completo
  FROM cuentas c INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente;
  ```
- [ ] Tener a mano (en papel o en un bloc de notas) los datos de prueba de esta página, para no inventarlos en vivo.
- [ ] Tener las capturas de evidencias abiertas en una carpeta, como plan B si algo falla.

## Orden de la demostración

| # | Qué se muestra | Datos que se usan | Qué decir | Tiempo |
|---|---|---|---|---|
| 1 | Workbench: las dos tablas y los datos de prueba | Consulta `SELECT * FROM clientes;` | La base tiene dos tablas relacionadas, y cada cuenta guarda el id del cliente dueño (clave foránea) | 45 s |
| 2 | Ejecutar `MenuPrincipal` y presionar "Administrar clientes" | — | El menú abre los dos módulos. La tabla se llena sola desde MySQL al abrir la ventana | 20 s |
| 3 | Registrar un cliente válido (CP-02) | CLI003 · 11111111-1 · Pedro Gomez · 7000-0003 · pedro.gomez@example.com · Santa Tecla | Antes de guardar se valida el formato y se revisa que el código y el DUI no estén repetidos. Todo cliente nuevo queda ACTIVO | 60 s |
| 4 | Validaciones (CP-04, CP-05, CP-08) | Formulario vacío → DUI `123456789` → DUI `12345678-9` (el de Ana) | El programa muestra todos los errores juntos y no guarda nada | 60 s |
| 5 | Modificar (CP-12) | Seleccionar a Pedro y cambiar el teléfono a 7000-0099 | Al hacer clic en la fila, los datos pasan al formulario | 30 s |
| 6 | Cambiar estado (CP-18) | Carlos Martinez → INACTIVO | No se borran clientes, solo cambia su estado | 30 s |
| 7 | En el menú, presionar "Registrar cuentas" y desplegar la lista de clientes (CP-21) | — | Carlos ya no aparece porque está INACTIVO: solo se crean cuentas para clientes activos | 30 s |
| 8 | Registrar una cuenta (CP-22) | CTA002 · AHORRO · Pedro Gomez | El sistema pone la fecha de hoy, saldo 0.00 (BigDecimal) y estado ACTIVA | 45 s |
| 9 | Número de cuenta repetido (CP-25) | CTA001 | No puede haber dos cuentas con el mismo número | 20 s |
| 10 | Segunda cuenta para Ana (CP-23) | CTA003 · CORRIENTE · Ana Lopez | Un cliente puede tener varias cuentas: relación uno a muchos | 30 s |
| 11 | Workbench: volver a ejecutar las consultas | Las dos consultas de arriba | Todo quedó guardado en MySQL, y el INNER JOIN muestra a cada cuenta con su dueño | 45 s |
| 12 | Cerrar y volver a abrir la aplicación (CP-29) | — | Los datos se conservan porque están en la base, no en memoria | 30 s |

## Si preguntan por las pruebas

- Se prepararon 31 casos de prueba. La primera ejecución encontró 3 errores (CP-07, CP-17 y CP-28): se corrigieron y en la ejecución final se aprobaron los 31.
- Ejemplo para contar: "Modificar cambiaba también el estado del cliente; la prueba CP-17 lo detectó y se corrigió en ClienteDAO".

## Si preguntan dónde está el "Eliminar" del CRUD

- La baja es lógica: en lugar de borrar, el cliente pasa a INACTIVO o BLOQUEADO con Cambiar estado (CP-18 y CP-19).
- Un cliente con cuentas no se puede borrar, porque la clave foránea de `cuentas` lo impide, y en la Fase 2 sus movimientos tienen que conservarse.
- Un cliente que no está ACTIVO deja de aparecer al crear cuentas (CP-21), así que en la práctica queda dado de baja.

## Si algo falla en vivo

- **No conecta a MySQL:** revisar que el servicio de MySQL esté encendido y que exista `src/db.properties` (sección 11.6 del documento técnico).
- **Sale un error que no se esperaba:** decirlo con calma, mostrar la captura de esa prueba (carpeta `docs/pruebas/evidencias`) y explicar qué debería ocurrir.
