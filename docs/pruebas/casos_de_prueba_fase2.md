# Casos de prueba – Fase 2

Responsable: David Salgado (documento técnico, pruebas y evidencias).

Estos casos salen de las reglas de negocio de la Fase 2 y de los datos del script SQL de `main` (commit `9709d31`). Las ventanas de depósito, retiro y transferencia todavía no existen, así que, cuando lleguen, se ajustarán los nombres de los botones y el texto exacto de los mensajes, sin cambiar lo que se comprueba.

## Resumen

| Ejecución | Fecha | Aprobadas | Fallidas |
|---|---|---|---|
| Pendiente: falta el módulo de transacciones | — | — | — |

## Cómo se ejecutan

1. Importar `database/banco_academico_udb.sql` en una base vacía.
2. **Regresión:** ejecutar en orden CP-01 a CP-31 de los [casos de la Fase 1](casos_de_prueba.md), para comprobar que lo que ya funcionaba no se rompió. Con el script nuevo cambian tres resultados esperados (ver abajo).
3. Ejecutar en orden CP-32 a CP-75. Cada prueba parte de los saldos que dejó la anterior.
4. Guardar cada captura en la carpeta que indica la columna "Evidencia", dentro de [evidencias/](evidencias/).

## Datos de prueba

El script nuevo agrega cuatro cuentas de Ana Lopez y tres movimientos:

| Cuenta | Tipo | Estado | Saldo | Movimientos del script |
|---|---|---|---|---|
| CTA-PRB-01 | AHORRO | ACTIVA | $1,300.00 | Depósito de $1,500.00 y transferencia enviada de $200.00 a CTA-PRB-02 |
| CTA-PRB-02 | CORRIENTE | ACTIVA | $1,000.00 | Depósito de $800.00 y transferencia recibida de $200.00 |
| CTA-PRB-03 | AHORRO | INACTIVA | $350.00 | Ninguno |
| CTA-PRB-04 | CORRIENTE | BLOQUEADA | $600.00 | Ninguno |

Al terminar la regresión de la Fase 1, la base queda así:

| Cliente | Estado | Cuentas |
|---|---|---|
| CLI001 Ana Lopez | ACTIVO | CTA001, CTA003 y las cuatro CTA-PRB |
| CLI002 Carlos Martinez | INACTIVO | — |
| CLI003 Pedro Gomez | ACTIVO | CTA002 |
| CLI004 Maria Rivas | BLOQUEADO | — |
| CLI005 Ana Banco | ACTIVO | — |
| CLI006 Ana Empresa | ACTIVO | — |

**Cambios en la regresión por el script nuevo:** en CP-23, Ana Lopez aparece con 6 cuentas, no con 2; en CP-27 la tabla tiene 7 cuentas, no 3; y en CP-29, al volver a abrir, hay 6 clientes y 7 cuentas.

No hace falta preparar nada más: CTA-PRB-03 (INACTIVA) y CTA-PRB-04 (BLOQUEADA) ya traen saldo, así que sirven para comprobar que el rechazo se debe al estado y no a la falta de dinero. `CTA999` no existe; se usa para probar cuentas inexistentes.

### Saldos esperados

Solo las operaciones exitosas cambian los saldos. CTA003, CTA-PRB-01 y CTA-PRB-02 no se tocan y CTA-PRB-03 y CTA-PRB-04 nunca deben cambiar.

| Después de | CTA001 | CTA002 | CTA-PRB-03 (INACTIVA) | CTA-PRB-04 (BLOQUEADA) |
|---|---|---|---|---|
| Regresión | $0.00 | $0.00 | $350.00 | $600.00 |
| CP-33 (depósito) | $100.00 | $0.00 | $350.00 | $600.00 |
| CP-34 (depósito) | $100.00 | $50.25 | $350.00 | $600.00 |
| CP-40 (retiro) | $70.00 | $50.25 | $350.00 | $600.00 |
| CP-42 (retiro) | $70.00 | $0.00 | $350.00 | $600.00 |
| CP-47 (transferencia) | **$44.50** | **$25.50** | **$350.00** | **$600.00** |

Totales al final:

| Total | Del script | De las pruebas | Esperado |
|---|---|---|---|
| Depositado | $2,300.00 (1,500.00 + 800.00) | $150.25 (100.00 + 50.25) | **$2,450.25** |
| Retirado | $0.00 | $80.25 (30.00 + 50.25) | **$80.25** |
| Transferido | $200.00 | $25.50 | **$225.50** |

## Tabla de casos de prueba

### Antes de los movimientos

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-32 | Reportes con los datos del script | Después de la regresión, sin hacer ninguna operación, presionar "Actualizar todos los reportes" y revisar cada pestaña | Totales: depositado $2,300.00, retirado $0.00 (no un espacio vacío ni "null") y transferido $200.00. Cuentas activas: CTA001, CTA-PRB-01, CTA-PRB-02, CTA002 y CTA003. Cuentas bloqueadas: solo CTA-PRB-04. Historial: los 3 movimientos del script. Transacciones por cuenta de CTA001: vacío, sin error | — | Pendiente | 11_reportes/CP-32a a f |

### Depósitos

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-33 | Depósito válido | Depositar $100.00 en CTA001. Revisar en MySQL | Mensaje de éxito. CTA001 pasa de $0.00 a $100.00 en la ventana y en MySQL, y se guarda un movimiento DEPOSITO de $100.00 con fecha y hora | — | Pendiente | 07_depositos/CP-33a y b |
| CP-34 | Depósito con centavos | Depositar $50.25 en CTA002 | CTA002 queda en $50.25 exactos, sin redondeos como 50.249999 | — | Pendiente | 07_depositos/CP-34 |
| CP-35 | Monto cero o negativo | Depositar $0.00 en CTA001. Repetir con -10.00 | Mensaje de que el monto debe ser mayor que cero. CTA001 sigue en $100.00 y no se guarda ningún movimiento | — | Pendiente | 07_depositos/CP-35a y b |
| CP-36 | Monto vacío o con letras | Depositar en CTA001 con el monto vacío. Repetir con `abc` | Mensaje claro de que el monto no es válido. El programa no se cierra ni muestra un error técnico (por ejemplo, NumberFormatException). CTA001 sigue en $100.00 | — | Pendiente | 07_depositos/CP-36a y b |
| CP-37 | Cuenta bloqueada | Depositar $10.00 en CTA-PRB-04 | Mensaje de que la cuenta no está activa. CTA-PRB-04 sigue en $600.00 y no se guarda ningún movimiento | — | Pendiente | 07_depositos/CP-37 |
| CP-38 | Cuenta inactiva | Depositar $10.00 en CTA-PRB-03 | Mensaje de que la cuenta no está activa. CTA-PRB-03 sigue en $350.00 | — | Pendiente | 07_depositos/CP-38 |
| CP-39 | Cuenta que no existe | Intentar depositar $10.00 en CTA999 | No se hace el depósito: o CTA999 no se puede elegir (si las cuentas se eligen de una lista), o aparece un mensaje de que la cuenta no existe. No se guarda ningún movimiento | — | Pendiente | 07_depositos/CP-39 |

### Retiros

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-40 | Retiro válido | Retirar $30.00 de CTA001. Revisar en MySQL | Mensaje de éxito. CTA001 pasa de $100.00 a $70.00 y se guarda un movimiento RETIRO de $30.00 | — | Pendiente | 08_retiros/CP-40a y b |
| CP-41 | Saldo insuficiente | Retirar $70.01 de CTA001 (un centavo más que el saldo) | Mensaje de saldo insuficiente. CTA001 sigue en $70.00 y no se guarda ningún movimiento | — | Pendiente | 08_retiros/CP-41 |
| CP-42 | Retirar todo el saldo | Retirar $50.25 de CTA002 | Se permite, porque el saldo alcanza justo. CTA002 queda en $0.00 | — | Pendiente | 08_retiros/CP-42 |
| CP-43 | Monto cero o negativo | Retirar $0.00 de CTA001. Repetir con -5.00 | Mensaje de que el monto debe ser mayor que cero. CTA001 sigue en $70.00 | — | Pendiente | 08_retiros/CP-43a y b |
| CP-44 | Cuenta bloqueada con saldo | Retirar $5.00 de CTA-PRB-04, que tiene $600.00 | Mensaje de que la cuenta no está activa, no de saldo insuficiente, porque saldo sí tiene. CTA-PRB-04 sigue en $600.00 | — | Pendiente | 08_retiros/CP-44 |
| CP-45 | Cuenta inactiva con saldo | Retirar $5.00 de CTA-PRB-03, que tiene $350.00 | Mensaje de que la cuenta no está activa. CTA-PRB-03 sigue en $350.00 | — | Pendiente | 08_retiros/CP-45 |
| CP-46 | Cuenta que no existe | Intentar retirar $5.00 de CTA999 | No se hace el retiro (igual que en CP-39) | — | Pendiente | 08_retiros/CP-46 |

### Transferencias

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-47 | Transferencia válida | Transferir $25.50 de CTA001 a CTA002. Revisar en MySQL | Mensaje de éxito. CTA001 pasa de $70.00 a $44.50 y CTA002 de $0.00 a $25.50. Se guarda un movimiento TRANSFERENCIA con la cuenta de origen, la de destino, el monto, la fecha y la hora | — | Pendiente | 09_transferencias/CP-47a y b |
| CP-48 | Saldo insuficiente | Transferir $44.51 de CTA001 a CTA002 | Mensaje de saldo insuficiente. CTA001 sigue en $44.50 y CTA002 en $25.50 | — | Pendiente | 09_transferencias/CP-48 |
| CP-49 | Monto cero o negativo | Transferir $0.00 de CTA001 a CTA002. Repetir con -5.00 | Mensaje de que el monto debe ser mayor que cero. Ningún saldo cambia | — | Pendiente | 09_transferencias/CP-49a y b |
| CP-50 | La cuenta de origen no existe | Transferir $5.00 de CTA999 a CTA002 | No se hace la transferencia. CTA002 sigue en $25.50 | — | Pendiente | 09_transferencias/CP-50 |
| CP-51 | La cuenta de destino no existe | Transferir $5.00 de CTA001 a CTA999 | No se hace la transferencia y **no se descuenta nada**: CTA001 sigue en $44.50 | — | Pendiente | 09_transferencias/CP-51 |
| CP-52 | Origen bloqueada | Transferir $10.00 de CTA-PRB-04 (BLOQUEADA, con $600.00) a CTA001 | Mensaje de que la cuenta de origen no está activa. CTA-PRB-04 sigue en $600.00 y CTA001 en $44.50 | — | Pendiente | 09_transferencias/CP-52 |
| CP-53 | Destino inactiva | Transferir $10.00 de CTA001 a CTA-PRB-03 (INACTIVA) | Mensaje de que la cuenta de destino no está activa. CTA001 sigue en $44.50 y CTA-PRB-03 en $350.00 | — | Pendiente | 09_transferencias/CP-53 |
| CP-54 | Misma cuenta de origen y destino | Transferir $10.00 de CTA001 a CTA001 | Se rechaza y no se guarda ningún movimiento (regla por confirmar con el grupo, pregunta 2) | — | Pendiente | 09_transferencias/CP-54 |
| CP-55 | Destino bloqueada con la ventana abierta | Dejar la ventana con CTA001 → CTA002 y $5.00, sin confirmar. En Workbench: `UPDATE cuentas SET estado = 'BLOQUEADA' WHERE numero_cuenta = 'CTA002';`. Volver y confirmar. Al terminar, devolver CTA002 a `'ACTIVA'` | Se rechaza: el sistema revisa el estado al momento de transferir, no solo al abrir la ventana (en la Fase 1 este caso causó el problema P-08). Ningún saldo cambia | — | Pendiente | 09_transferencias/CP-55 |

### Historial y consulta de movimientos

El historial se muestra del movimiento más reciente al más antiguo.

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-56 | Historial de una cuenta | Mostrar el historial de CTA001 | 3 movimientos: transferencia enviada de $25.50 a CTA002, retiro de $30.00 y depósito de $100.00. No aparece ninguna operación rechazada | — | Pendiente | 10_historial/CP-56 |
| CP-57 | Historial con una transferencia recibida | Mostrar el historial de CTA002 | Transferencia recibida de $25.50 desde CTA001, retiro de $50.25 y depósito de $50.25 | — | Pendiente | 10_historial/CP-57 |
| CP-58 | Consultar un movimiento | Consultar la transferencia de CP-47 | Muestra el tipo, el monto ($25.50), la fecha y hora, la cuenta de origen (CTA001) y la de destino (CTA002) | — | Pendiente | 10_historial/CP-58 |
| CP-59 | Cuenta sin movimientos | Mostrar el historial de CTA003 | Tabla vacía o aviso de que no tiene movimientos, sin error | — | Pendiente | 10_historial/CP-59 |
| CP-60 | El saldo cuadra con los movimientos | En MySQL, para cada cuenta, sumar depósitos y transferencias recibidas y restar retiros y transferencias enviadas | El resultado es igual al saldo guardado en todas las cuentas. Con el script actual fallará en CTA-PRB-03 y CTA-PRB-04 (ver observación 1) | — | Pendiente | 10_historial/CP-60 |

### Reportes

En cada reporte se toma la captura y se compara con la consulta correspondiente de `database/consultas_reportes.sql`.

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-61 | Cuentas activas | Pestaña "Cuentas Activas", tipo TODOS | CTA001, CTA-PRB-01, CTA-PRB-02, CTA002 y CTA003, con su cliente y saldo. No aparecen CTA-PRB-03 ni CTA-PRB-04 | — | Pendiente | 11_reportes/CP-61 |
| CP-62 | Cuentas bloqueadas | Pestaña "Cuentas Bloqueadas", tipo TODOS | Solo CTA-PRB-04 (Ana Lopez, $600.00). CTA-PRB-03 no aparece, porque está INACTIVA, no BLOQUEADA | — | Pendiente | 11_reportes/CP-62 |
| CP-63 | Clientes registrados | Pestaña "Clientes Registrados", estado TODOS | Los 6 clientes con su estado: Ana Lopez, Pedro Gomez, Ana Banco y Ana Empresa ACTIVO; Carlos Martinez INACTIVO; Maria Rivas BLOQUEADO | — | Pendiente | 11_reportes/CP-63 |
| CP-64 | Historial de transacciones | Pestaña "Historial de Transacciones", tipo TODOS | 8 movimientos, del más reciente al más antiguo: transferencia CTA001 → CTA002 $25.50, retiro CTA002 $50.25, retiro CTA001 $30.00, depósito CTA002 $50.25, depósito CTA001 $100.00, transferencia CTA-PRB-01 → CTA-PRB-02 $200.00, depósito CTA-PRB-02 $800.00 y depósito CTA-PRB-01 $1,500.00. Ninguna operación rechazada | — | Pendiente | 11_reportes/CP-64 |
| CP-65 | Transacciones por cuenta | Pestaña "Transacciones por Cuenta", elegir CTA001. Repetir con CTA-PRB-02 | CTA001: los 3 movimientos de CP-56. CTA-PRB-02: la transferencia recibida de $200.00 y el depósito de $800.00 | — | Pendiente | 11_reportes/CP-65a y b |
| CP-66 | Total depositado | Pestaña "Totales", sin filtro de fechas | $2,450.25 | — | Pendiente | 11_reportes/CP-66 |
| CP-67 | Total retirado | Pestaña "Totales", sin filtro de fechas | $80.25 | — | Pendiente | 11_reportes/CP-66 (misma captura) |
| CP-68 | Total transferido | Pestaña "Totales", sin filtro de fechas | $225.50 | — | Pendiente | 11_reportes/CP-66 (misma captura) |

### Log4J

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-69 | Se genera el archivo de logs | Cerrar la aplicación y abrir `logs/banco-academico.log`, en la carpeta desde donde se ejecutó el programa (en IntelliJ, la carpeta del proyecto) | El archivo existe y cada línea tiene fecha y hora, hilo, nivel (INFO, WARN o ERROR), clase y mensaje | — | Pendiente | 12_logs/CP-69 y copia del archivo |
| CP-70 | Las operaciones exitosas quedan en el log | Buscar en el log las operaciones de CP-33, CP-40 y CP-47 | Aparece cada una con nivel INFO, el tipo, el monto y el saldo resultante | — | Pendiente | 12_logs/CP-70 |
| CP-71 | Las operaciones rechazadas quedan en el log | Buscar en el log las de CP-41 (saldo insuficiente) y CP-37 (cuenta bloqueada) | Aparecen con nivel WARN y el motivo del rechazo | — | Pendiente | 12_logs/CP-71 |
| CP-72 | El log no muestra la contraseña | Buscar en el log la contraseña que está en `db.properties` | No aparece | — | Pendiente | 12_logs/CP-72 |

La carpeta `logs/` está en `.gitignore`, así que el archivo de logs que se entrega se copia a mano a `evidencias/12_logs/`.

### Pruebas generales

| ID | Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| CP-73 | MySQL se apaga durante una operación | Con la ventana de transacciones abierta, detener MySQL e intentar depositar $10.00 en CTA001. Al terminar, volver a encenderlo | Mensaje de error de conexión y el programa no se cierra. CTA001 sigue en $44.50. En el log queda un ERROR con la excepción | — | Pendiente | 05_validaciones/CP-73a y b |
| CP-74 | Saldos y movimientos se conservan | Cerrar y volver a abrir la aplicación | Los saldos y el historial están igual que antes de cerrar | — | Pendiente | 02_consulta/CP-74 |
| CP-75 | Revisión del código de la transferencia | Revisar el método que hace la transferencia | El descuento, el abono y el movimiento se guardan en una sola transacción de la base (`setAutoCommit(false)`, `commit` y, si algo falla, `rollback`), y los montos usan `BigDecimal`, nunca `double` | — | Pendiente | Fragmento de código en el documento técnico |

## Qué regla comprueba cada prueba

| Operación | Regla de negocio | Pruebas |
|---|---|---|
| Depósito | La cuenta debe existir | CP-39 |
| | La cuenta debe estar activa | CP-37, CP-38 |
| | El monto debe ser mayor que cero | CP-35, CP-36 |
| | El saldo aumenta | CP-33, CP-34 |
| Retiro | La cuenta debe existir | CP-46 |
| | La cuenta debe estar activa | CP-44, CP-45 |
| | El monto debe ser mayor que cero | CP-43 |
| | El saldo debe ser suficiente | CP-41, CP-42 |
| | El saldo disminuye | CP-40, CP-42 |
| Transferencia | La cuenta de origen debe existir | CP-50 |
| | La cuenta de destino debe existir | CP-51 |
| | Ambas cuentas deben estar activas | CP-52, CP-53, CP-55 |
| | El monto debe ser mayor que cero | CP-49 |
| | La cuenta de origen debe tener saldo suficiente | CP-48 |
| | Se descuenta de la de origen y se suma a la de destino | CP-47, CP-60, CP-75 |
| Movimientos | Consultar un movimiento | CP-58 |
| | Mostrar el historial | CP-56, CP-57, CP-59 |
| Reportes | Los 8 reportes de la guía | CP-61 a CP-68 (y CP-32 con los datos del script) |
| Excepciones | Los errores se controlan y se muestran con un mensaje | CP-36, CP-73 |
| Log4J | Se registran los eventos y se genera el archivo de logs | CP-69 a CP-73 |

## Observaciones al revisar el código

Se encontraron al preparar las pruebas, antes de ejecutarlas. Cuando se confirmen, pasan al [registro de problemas](registro_de_problemas.md).

1. **CTA-PRB-03 y CTA-PRB-04 tienen saldo, pero ningún movimiento** ($350.00 y $600.00). Sus saldos no se explican con el historial y la suma de los saldos no cuadra con los totales. Solución sugerida: que el script les agregue un depósito inicial, con fecha anterior al cambio de estado.
2. **En una transferencia, `saldo_resultante` solo guarda el saldo de la cuenta de origen.** En el historial de la cuenta que recibe, la columna muestra el saldo de la otra cuenta. Por ejemplo, la transferencia del script aparece en CTA-PRB-02 con $1,300.00, que es el saldo de CTA-PRB-01. Hay que decidir si se agrega una columna para el saldo de destino o si esa columna no se muestra en las transferencias recibidas.

## Decisiones que faltan (preguntar al grupo)

1. **Cambiar el estado de una cuenta.** El sistema no tiene esa opción. Las pruebas usan las cuentas del script y Workbench (CP-55), pero conviene decidir si se agrega a la ventana de cuentas.
2. **Transferir a la misma cuenta.** Se recomienda rechazarlo (CP-54).
3. **Montos con más de dos decimales**, como 10.555. Se recomienda rechazarlos en lugar de redondearlos. También falta acordar el monto máximo: `DECIMAL(12,2)` admite hasta 9,999,999,999.99.
4. **Estado del cliente.** Las reglas solo hablan del estado de la cuenta. ¿Se puede depositar en una cuenta ACTIVA cuyo cliente está INACTIVO o BLOQUEADO?
5. **Operaciones rechazadas.** Se recomienda que no se guarden como movimientos y que queden solo en el log, con nivel WARN.

Ya resueltas al revisar el código: la transferencia se guarda como un solo movimiento con origen y destino; el log queda en `logs/banco-academico.log`; los reportes se ven en pantalla, por pestañas, sin exportar; y "Clientes registrados" incluye todos los estados.
