# Prueba integrada – Fase 1

Recorrido completo del sistema con la aplicación abierta desde `MenuPrincipal` y la base real. Está ordenado para grabarse en un solo video.

| Dato | Valor |
|---|---|
| Fecha | |
| Realizada por | David Salgado |
| Equipo | Linux Mint 22.3, NetBeans 31, JDK 21, MySQL 8.0.46 |
| Resultado | __ de 25 pasos correctos |

## Antes de grabar

1. Dejar la base solo con los datos de prueba:
   ```bash
   sudo mysql --defaults-file=/etc/mysql/debian.cnf -e "DROP DATABASE banco_academico_udb;"
   sudo mysql --defaults-file=/etc/mysql/debian.cnf < ~/Downloads/POOUDB2026-ProyectoFinal-Grupo7/database/banco_academico_udb.sql
   ```
2. En NetBeans: Clean and Build del proyecto.
3. Abrir una terminal con MySQL (contraseña `ClaveUDB2026*`):
   ```bash
   mysql -u udb_general -p banco_academico_udb
   ```
   Consultas que se usan en el video:
   ```sql
   SELECT id_cliente, codigo_cliente, nombre_completo, estado FROM clientes;
   SELECT c.numero_cuenta, c.tipo_cuenta, c.fecha_apertura, c.saldo, c.estado, cl.nombre_completo
   FROM cuentas c JOIN clientes cl ON c.id_cliente = cl.id_cliente;
   ```
4. En OBS, agregar la fuente "Captura de pantalla" y presionar "Iniciar grabación".
5. Capturas: Alt+Impr Pant captura la ventana activa. Se guardan en `docs/pruebas/evidencias/prueba_integrada/` con el nombre del paso (`PI-05.png`).

## Recorrido

| # | Qué hacer | Qué debe verse | ✔ | Captura |
|---|---|---|---|---|
| **1. Base de datos** | | | | |
| PI-01 | En la terminal, ejecutar las dos consultas | Ana Lopez y Carlos Martinez ACTIVO; la cuenta CTA001 es de Ana | | PI-01 |
| **2. Arranque** | | | | |
| PI-02 | En NetBeans, ejecutar `vista.MenuPrincipal` (F6) | Menú con "Administrar clientes", "Registrar cuentas" y "Salir" | | PI-02 |
| PI-03 | Presionar "Administrar clientes" | La tabla muestra CLI001 y CLI002 | | |
| PI-04 | Volver al menú y presionar "Registrar cuentas". Dejar las dos ventanas abiertas | Se abre la ventana de cuentas con CTA001 de Ana Lopez. El menú sigue abierto | | PI-04 |
| **3. Clientes** | | | | |
| PI-05 | Registrar CLI003 · 11111111-1 · Pedro Gomez · 7000-0003 · pedro.gomez@example.com · Santa Tecla | "Cliente registrado exitosamente."; Pedro aparece con estado ACTIVO | | PI-05 |
| PI-06 | Registrar CLI004 · 22222222-2 · Maria Rivas · 7000-0004 · maria.rivas@example.com · Soyapango, eligiendo BLOQUEADO en la lista de estado | Maria queda ACTIVO | | |
| PI-07 | Con el formulario vacío, presionar Registrar | Un mensaje con los 6 campos obligatorios | | PI-07 |
| PI-08 | Llenar CLI008 · `123456789` · Prueba Datos · 7000-0008 · prueba@example.com · Ilopango y presionar Registrar | "El DUI debe tener el formato 00000000-0…" | | PI-08 |
| PI-09 | Sobre el mismo formulario: DUI `88888888-8` y correo `anacorreo.com` | "El correo electrónico ingresado no tiene un formato válido." | | PI-09 |
| PI-10 | Correo `prueba@example.com`, código `CLI007` y DUI `12345678-9` (el de Ana) | "El DUI ya está registrado en la BD." | | PI-10 |
| PI-11 | Código `CLI001` y DUI `55555555-5` | "El código de cliente ya existe en la BD." | | PI-11 |
| PI-12 | Clic en Pedro, cambiar el teléfono a `7000-0099` y presionar Modificar | "Cliente modificado exitosamente."; el teléfono cambia en la tabla | | PI-12 |
| PI-13 | Clic en Pedro, elegir INACTIVO en la lista de estado y presionar **Modificar** | Pedro sigue ACTIVO | | PI-13 |
| PI-14 | Clic en Carlos, elegir INACTIVO y presionar **Cambiar estado** | "Estado actualizado a INACTIVO."; Carlos sigue en la tabla | | PI-14 |
| PI-15 | Clic en Maria, elegir BLOQUEADO y presionar Cambiar estado | Maria sigue en la tabla con estado BLOQUEADO | | |
| **4. Cuentas** | | | | |
| PI-16 | Pasar a la ventana de cuentas y desplegar la lista de clientes | Aparecen Ana Lopez y Pedro Gomez. No aparecen Carlos (INACTIVO) ni Maria (BLOQUEADO) | | PI-16 |
| PI-17 | Registrar `CTA002`, AHORRO, Pedro Gomez | "Cuenta registrada exitosamente."; saldo $0.00, ACTIVA, fecha de hoy, Pedro Gomez | | PI-17 |
| PI-18 | Registrar `CTA003`, CORRIENTE, Ana Lopez | Ana tiene dos cuentas: CTA001 y CTA003 | | PI-18 |
| PI-19 | Escribir `CTA001` y presionar Registrar Cuenta | "El número de cuenta ya existe en la base de datos." | | PI-19 |
| PI-20 | Dejar vacío el número y presionar Registrar Cuenta | "El número de cuenta es obligatorio." | | |
| **5. Las dos ventanas juntas** | | | | |
| PI-21 | En cuentas, elegir a Pedro. En clientes, cambiar a Pedro a INACTIVO. Volver a cuentas, escribir `CTA004` y presionar Registrar Cuenta | Pedro ya no está en la lista y la cuenta no se crea. Al terminar, devolver a Pedro a ACTIVO | | PI-21 |
| PI-22 | En clientes, registrar CLI005 · 33333333-3 · Ana Banco · 7000-0005 · ana+banco@correo.com · San Miguel. Volver a cuentas y desplegar la lista | El correo con "+" se acepta y Ana Banco aparece en la lista de cuentas sin cerrar la ventana | | PI-22 |
| **6. Los datos se conservan** | | | | |
| PI-23 | En el menú, presionar Salir. Ejecutar otra vez `MenuPrincipal` y abrir las dos ventanas | Siguen los 5 clientes (Pedro con 7000-0099) y las 3 cuentas | | PI-23 |
| PI-24 | En la terminal, repetir las dos consultas | MySQL muestra lo mismo que las ventanas | | PI-24 |
| **7. Sin conexión** (opcional) | | | | |
| PI-25 | `sudo systemctl stop mysql` y abrir la aplicación. Después, `sudo systemctl start mysql` | Mensaje "Communications link failure"; la aplicación no se cierra | | PI-25 |

## Al terminar

- Detener la grabación en OBS.
- Volver a dejar la base limpia con los dos comandos de "Antes de grabar".
- Anotar en el [registro de problemas](registro_de_problemas.md) cualquier paso que no haya salido como se esperaba.
