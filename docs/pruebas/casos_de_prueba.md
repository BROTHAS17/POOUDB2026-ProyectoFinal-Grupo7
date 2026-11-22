# Casos de prueba – Fase 1

Responsable: David Salgado (pruebas, evidencias e integración).

## Resumen

| Ejecución | Fecha | Aprobadas | Fallidas |
|---|---|---|---|
| Código original de `main` (commit `b1cf533`) | 2026-11-08 | 25 de 28 (CP-01 a CP-28) | 3: CP-07, CP-17 y CP-28 |
| **Código corregido** (rama `feature/pruebas-integracion`) | **2026-11-08** | **31 de 31** | **0** |

Las 3 pruebas que fallaron con el código original descubrieron los problemas P-03, P-02 y P-08 del [registro de problemas](registro_de_problemas.md). Después de corregirlos, las 31 se aprobaron.

## Cómo se ejecutaron

- Con OpenJDK 21.0.12, MySQL Server 8.0.46 y Connector/J 9.7.0, en orden y sobre una base recién importada.
- Un programa de prueba operó las ventanas reales: escribió los datos, presionó los botones, comparó el mensaje con el resultado esperado y revisó MySQL. Las consultas están en [evidencias/consultas_mysql.txt](evidencias/consultas_mysql.txt).
- Las capturas están en [evidencias/](evidencias/), en la carpeta que indica la columna "Evidencia". Para repetir una prueba a mano, se sigue la columna "Pasos".

## Datos de prueba

Datos que trae el script SQL al importarlo:

| id | Código | DUI | Nombre | Estado | Cuentas |
|---|---|---|---|---|---|
| 1 | CLI001 | 12345678-9 | Ana Lopez | ACTIVO | CTA001 (AHORRO) |
| 2 | CLI002 | 98765432-1 | Carlos Martinez | ACTIVO | — |

Datos nuevos que se usan en las pruebas:

| Código | DUI | Nombre | Teléfono | Correo | Dirección |
|---|---|---|---|---|---|
| CLI003 | 11111111-1 | Pedro Gomez | 7000-0003 | pedro.gomez@example.com | Santa Tecla |
| CLI004 | 22222222-2 | Maria Rivas | 7000-0004 | maria.rivas@example.com | Soyapango |
| CLI005 | 33333333-3 | Ana Banco | 7000-0005 | ana+banco@correo.com | San Miguel |
| CLI006 | 44444444-4 | Ana Empresa | 7000-0006 | ana@empresa.company | La Libertad |

## Tabla de casos de prueba

### Módulo de clientes

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-01 | Consultar clientes al abrir | Abrir la ventana de clientes | La tabla muestra a CLI001 y CLI002, ordenados por ID | La tabla muestra CLI001 y CLI002, ordenados por ID | Aprobada | 02_consulta/CP-01 |
| CP-02 | Registrar cliente válido | Llenar el formulario con los datos de CLI003 y presionar Registrar. Revisar en MySQL | Mensaje "Cliente registrado exitosamente.", se limpia el formulario y CLI003 aparece en la tabla con estado ACTIVO. También queda en MySQL | Mensaje "Cliente registrado exitosamente." El formulario se limpió y CLI003 aparece en la tabla y en MySQL con estado ACTIVO | Aprobada | 01_registro/CP-02a, b y c |
| CP-03 | Todo cliente nuevo queda ACTIVO | Llenar los datos de CLI004, elegir BLOQUEADO en la lista de estado y presionar Registrar | Se guarda con estado ACTIVO, no BLOQUEADO | Se guardó con estado ACTIVO | Aprobada | 01_registro/CP-03 |
| CP-04 | Campos vacíos | Con el formulario vacío, presionar Registrar | Un solo mensaje con 6 errores: código, DUI, nombre, teléfono, correo y dirección obligatorios. No se guarda nada | Mensaje "Errores de Validación" con los 6 errores. No se guardó nada | Aprobada | 05_validaciones/CP-04 |
| CP-05 | DUI con formato incorrecto | Llenar los datos de un cliente nuevo con DUI `123456789` y presionar Registrar. Repetir con `1234567A-9` | Mensaje "El DUI debe tener el formato 00000000-0…". No se guarda | En los dos casos: "El DUI debe tener el formato 00000000-0 (8 dígitos, guion y 1 dígito)." No se guardó | Aprobada | 05_validaciones/CP-05a y b |
| CP-06 | Correo inválido | Llenar los datos de un cliente nuevo con correo `anacorreo.com` y presionar Registrar. Repetir con `ana@` | Mensaje "El correo electrónico ingresado no tiene un formato válido." No se guarda | En los dos casos aparece ese mensaje. No se guardó | Aprobada | 05_validaciones/CP-06a y b |
| CP-07 | Correo con `+` o dominio largo | Registrar CLI005 (correo `ana+banco@correo.com`) y luego CLI006 (correo `ana@empresa.company`) | Ambos correos se aceptan y los clientes se guardan | Los dos se registraron ("Cliente registrado exitosamente."). *Con el código original los dos correos se rechazaban: problema P-03, ya corregido* | Aprobada | 05_validaciones/CP-07a y b |
| CP-08 | DUI repetido al registrar | Registrar un cliente con código `CLI007` y DUI `12345678-9` (el de Ana Lopez) | Mensaje "El DUI ya está registrado en la BD." No se guarda | Mensaje "El DUI ya está registrado en la BD." No se guardó | Aprobada | 05_validaciones/CP-08 |
| CP-09 | Código repetido al registrar | Registrar un cliente con código `CLI001` y DUI `55555555-5` | Mensaje "El código de cliente ya existe en la BD." No se guarda | Mensaje "El código de cliente ya existe en la BD." No se guardó | Aprobada | 05_validaciones/CP-09 |
| CP-10 | Largo máximo | Registrar un cliente con el código `CLI0000000000001` (16 caracteres) | Mensaje "El código de cliente no puede exceder los 15 caracteres." No se guarda | Mensaje "El código de cliente no puede exceder los 15 caracteres." No se guardó | Aprobada | 05_validaciones/CP-10 |
| CP-11 | Seleccionar un cliente | Hacer clic en la fila de Pedro Gomez | Sus datos pasan al formulario, con el ID y el estado | El formulario muestra ID 3, CLI003, Pedro Gomez y estado ACTIVO | Aprobada | 03_modificacion/CP-11 |
| CP-12 | Modificar teléfono | Con Pedro seleccionado, cambiar el teléfono a `7000-0099` y presionar Modificar. Revisar en MySQL | Mensaje "Cliente modificado exitosamente." El teléfono nuevo se ve en la tabla y en MySQL | Mensaje "Cliente modificado exitosamente." El teléfono 7000-0099 aparece en la tabla y en MySQL | Aprobada | 03_modificacion/CP-12a y b |
| CP-13 | Modificar sin cambiar nada | Seleccionar a Pedro y presionar Modificar sin editar nada | Mensaje de éxito, no de error | Mensaje "Cliente modificado exitosamente." | Aprobada | 03_modificacion/CP-13 |
| CP-14 | Modificar sin seleccionar | Con el formulario limpio (después de CP-13), presionar Modificar | Aviso "Seleccione un cliente de la tabla para modificar." | Aparece ese aviso | Aprobada | 05_validaciones/CP-14 |
| CP-15 | Modificar con el DUI de otro cliente | Seleccionar a Pedro, poner el DUI `12345678-9` y presionar Modificar | Mensaje "El DUI pertenece a otro cliente." No se guarda | Mensaje "El DUI pertenece a otro cliente." El DUI de Pedro en MySQL sigue siendo 11111111-1 | Aprobada | 05_validaciones/CP-15 |
| CP-16 | Modificar con el código de otro cliente | Seleccionar a Pedro, poner el código `CLI001` y presionar Modificar | Mensaje "El código pertenece a otro cliente." No se guarda | Mensaje "El código pertenece a otro cliente." Pedro conserva el código CLI003 | Aprobada | 05_validaciones/CP-16 |
| CP-17 | Modificar no cambia el estado | Seleccionar a Pedro (ACTIVO), elegir INACTIVO en la lista de estado y presionar **Modificar** | Pedro sigue ACTIVO; solo el botón Cambiar estado cambia el estado | Pedro sigue ACTIVO en la tabla y en MySQL. *Con el código original quedaba INACTIVO: problema P-02, ya corregido* | Aprobada | 03_modificacion/CP-17a y b |
| CP-18 | Cambiar estado a INACTIVO | Seleccionar a Carlos Martinez, elegir INACTIVO y presionar Cambiar estado | Mensaje "Estado actualizado a INACTIVO." Carlos sigue en la tabla y en MySQL, con estado INACTIVO | Mensaje "Estado actualizado a INACTIVO." Carlos sigue en la tabla y en MySQL con estado INACTIVO | Aprobada | 04_cambio_estado/CP-18a y b |
| CP-19 | Cambiar estado a BLOQUEADO | Seleccionar a Maria Rivas, elegir BLOQUEADO y presionar Cambiar estado | Maria queda BLOQUEADO y sigue en la tabla | Mensaje "Estado actualizado a BLOQUEADO." Maria sigue en la tabla con estado BLOQUEADO | Aprobada | 04_cambio_estado/CP-19 |
| CP-20 | Cambiar estado sin seleccionar | Con el formulario limpio (después de CP-19), presionar Cambiar estado | Aviso "Seleccione un cliente de la tabla para cambiar su estado." | Aparece ese aviso | Aprobada | 04_cambio_estado/CP-20 |

### Módulo de cuentas

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-21 | Solo aparecen clientes activos | Abrir la ventana de cuentas y desplegar la lista de clientes | La lista muestra solo a los clientes ACTIVO. No aparecen Carlos (INACTIVO) ni Maria (BLOQUEADO) | La lista muestra Ana Lopez, Pedro Gomez, Ana Banco y Ana Empresa, que son los clientes ACTIVO en MySQL | Aprobada | 06_relacion_clientes_cuentas/CP-21 |
| CP-22 | Registrar cuenta válida | Escribir `CTA002`, elegir AHORRO y a Pedro Gomez, y presionar Registrar Cuenta. Revisar en MySQL | Mensaje "Cuenta registrada exitosamente." En la tabla: saldo $0.00, estado ACTIVA, fecha de hoy y cliente Pedro Gomez | Mensaje "Cuenta registrada exitosamente." En la tabla: CTA002, AHORRO, 2026-11-08, $0.00, ACTIVA, Pedro Gomez. En MySQL la fecha es la misma | Aprobada | 01_registro/CP-22a y b |
| CP-23 | Un cliente con varias cuentas | Registrar `CTA003`, CORRIENTE, para Ana Lopez | Ana aparece con dos cuentas (CTA001 y CTA003): relación uno a muchos | Ana Lopez aparece con 2 cuentas: CTA001 y CTA003 | Aprobada | 06_relacion_clientes_cuentas/CP-23 |
| CP-24 | Número de cuenta vacío | Dejar vacío el número y presionar Registrar Cuenta | Mensaje "El número de cuenta es obligatorio." No se guarda | Mensaje "El número de cuenta es obligatorio." No se guardó | Aprobada | 05_validaciones/CP-24 |
| CP-25 | Número de cuenta repetido | Escribir `CTA001` y presionar Registrar Cuenta | Mensaje "El número de cuenta ya existe en la base de datos." No se guarda | Mensaje "El número de cuenta ya existe en la base de datos." No se guardó | Aprobada | 05_validaciones/CP-25 |
| CP-26 | Número de cuenta demasiado largo | Escribir `CTA000000000000000001` (21 caracteres) y presionar Registrar Cuenta | Mensaje "El número de cuenta no puede exceder los 20 caracteres." No se guarda | Mensaje "El número de cuenta no puede exceder los 20 caracteres." No se guardó | Aprobada | 05_validaciones/CP-26 |
| CP-27 | Consultar cuentas con su dueño | Presionar Actualizar Lista y comparar con la consulta `SELECT … FROM cuentas c INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente` | La tabla de la ventana coincide con MySQL y cada cuenta muestra el nombre de su dueño | Las 3 cuentas coinciden con MySQL, incluida CTA001 con fecha 2026-11-05 | Aprobada | 06_relacion_clientes_cuentas/CP-27 |
| CP-28 | Cliente inactivado con la ventana de cuentas abierta | Dejar abierta la ventana de cuentas con Pedro elegido. En la de clientes, cambiar a Pedro a INACTIVO. Volver a cuentas y registrar `CTA004` | No se permite registrar la cuenta, porque una cuenta solo se crea para un cliente ACTIVO (regla 5.4). Al terminar, devolver a Pedro a ACTIVO | Mensaje "El cliente seleccionado ya no está ACTIVO…". CTA004 no se guardó y Pedro desapareció de la lista. *Con el código original la cuenta SÍ se guardaba: problema P-08, ya corregido* | Aprobada | 06_relacion_clientes_cuentas/CP-28a y b |

### Pruebas generales

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-29 | Los datos se conservan | Cerrar la aplicación, volver a abrirla con `vista.MenuPrincipal` y abrir clientes y cuentas desde el menú | Siguen todos los cambios de las pruebas anteriores | Al abrir de nuevo: 6 clientes (Pedro con el teléfono 7000-0099) y 3 cuentas (CTA001, CTA002, CTA003) | Aprobada | 02_consulta/CP-29a, b y c |
| CP-30 | Falta db.properties | Renombrar `src/db.properties`, hacer Build > Rebuild Project y abrir la aplicación. Al terminar, devolverle su nombre | Mensaje claro de que no se encontró db.properties; el programa no se cierra de golpe | Mensaje "Error al cargar clientes: Error al leer db.properties: No se encontró el archivo db.properties en el classpath." La ventana sigue abierta | Aprobada | 05_validaciones/CP-30 |
| CP-31 | MySQL apagado | Detener el servicio de MySQL y abrir la aplicación. Al terminar, encenderlo de nuevo | Mensaje de error de conexión; el programa no se cierra de golpe | Mensaje "Error al cargar clientes: Communications link failure…". La ventana sigue abierta | Aprobada | 05_validaciones/CP-31 |

## Pendiente

- Comparar esta lista con las 8 pruebas de la sección 11 de la guía del grupo, cuando el grupo la comparta.
