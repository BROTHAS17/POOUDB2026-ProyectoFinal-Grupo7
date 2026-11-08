# Banco Académico UDB – Módulo de Clientes (Integrante 2)

Programa de escritorio en Java (Swing) que administra clientes en MySQL mediante JDBC.

## Estructura

```
Modulo_Clientes_UDB/
├── .gitignore               (ignora db.properties)
├── db.properties.example    (plantilla; se copia como db.properties)
└── src/
    ├── modelo/Cliente.java
    ├── conexion/Conexion.java
    ├── dao/ClienteDAO.java
    ├── validacion/ValidadorCliente.java
    └── vista/VentanaClientes.java
```

Si el grupo usa un paquete base (por ejemplo `sv.edu.udb.xxx`), hay que anteponerlo en cada `package` y en los `import`.

## Herramientas (según la guía del grupo)

Java JDK 21, IntelliJ IDEA, MySQL Server 8.4 LTS, MySQL Workbench 8.0.x, MySQL Connector/J 9.7.0.

## Cómo ejecutarlo

1. El Integrante 1 entrega el script `.sql`. Impórtalo en tu MySQL local para tener la base `banco_academico_udb` con la tabla `clientes`.
2. Copia `src/` dentro de un proyecto de IntelliJ (JDK 21).
3. Agrega `mysql-connector-j-9.7.0.jar` al proyecto: File > Project Structure > Libraries > + > Java.
4. En la **raíz del proyecto** copia `db.properties.example` como `src/db.properties` y escribe tu usuario y contraseña de MySQL. Este archivo **no se sube** a Git (ya está en `.gitignore`; confírmalo antes del primer commit).
5. Ejecuta `vista.VentanaClientes`. El directorio de trabajo debe ser la raíz del proyecto (es el valor por defecto de IntelliJ); si no, `Conexion` no encontrará `src/db.properties`.

## Datos de ejemplo del script SQL

El script del Integrante 1 inserta dos clientes (`CLI001` con DUI `12345678-9` y `CLI002` con DUI `98765432-1`) y una cuenta (`CTA001`). Úsalos para las pruebas de duplicados y no los repitas al registrar clientes nuevos.

El script no borra la base si ya existe: al volver a importarlo, `CREATE TABLE` falla porque las tablas ya están. Para reimportar, elimina antes la base `banco_academico_udb` en tu MySQL local.

## Comportamiento acordado

- El estado inicial de un cliente siempre es ACTIVO.
- **Modificar** actualiza código, DUI, nombre, teléfono, correo y dirección. **No** cambia el estado.
- **Cambiar estado** es el único botón que modifica el estado (ACTIVO, INACTIVO o BLOQUEADO).
- No se borran clientes: solo cambia su estado.
- Código y DUI no pueden repetirse (se verifica antes de guardar).
- Validaciones: campos obligatorios, longitudes máximas según la tabla, DUI con formato `00000000-0` (solo formato, sin dígito verificador) y correo con formato básico.

## Casos de prueba

Los resultados obtenidos se llenan después de ejecutar el programa contra MySQL. Hasta entonces quedan como [PENDIENTE DE VERIFICAR].

| Prueba | Pasos | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|
| Registrar cliente válido | Usar un código y un DUI que **no** estén en los datos de ejemplo (por ejemplo `CLI003` y `11111111-1`) y presionar Registrar | Se guarda con estado ACTIVO y aparece en la tabla | [PENDIENTE DE VERIFICAR] | Pendiente |
| Campos vacíos | Dejar campos en blanco y presionar Registrar | Mensaje con los errores; no se guarda | [PENDIENTE DE VERIFICAR] | Pendiente |
| DUI con formato inválido | Escribir `123456789` y presionar Registrar | Mensaje de formato; no se guarda | [PENDIENTE DE VERIFICAR] | Pendiente |
| DUI repetido | Registrar un cliente nuevo (código `CLI003`) con el DUI `12345678-9`, que ya existe en los datos de ejemplo | Mensaje de DUI duplicado; no se guarda | [PENDIENTE DE VERIFICAR] | Pendiente |
| Código repetido | Registrar un cliente con el código `CLI001` (ya existe en los datos de ejemplo) y un DUI nuevo | Mensaje de código duplicado; no se guarda | [PENDIENTE DE VERIFICAR] | Pendiente |
| Correo con `+` o dominio largo | Registrar con `ana+banco@correo.com` o `ana@empresa.company` | Se acepta el correo | [PENDIENTE DE VERIFICAR] | Pendiente |
| Modificar teléfono | Seleccionar una fila, cambiar el teléfono y presionar Modificar | El nuevo teléfono se ve en la tabla y en MySQL | [PENDIENTE DE VERIFICAR] | Pendiente |
| Modificar sin cambiar nada | Seleccionar una fila y presionar Modificar sin editar | Mensaje de éxito (no error) | [PENDIENTE DE VERIFICAR] | Pendiente |
| Modificar con DUI de otro cliente | Poner el DUI de otro cliente y presionar Modificar | Mensaje de DUI duplicado | [PENDIENTE DE VERIFICAR] | Pendiente |
| Cambiar a INACTIVO | Seleccionar un cliente, elegir INACTIVO y presionar Cambiar estado | El estado cambia y el cliente sigue en la base | [PENDIENTE DE VERIFICAR] | Pendiente |
| Modificar no cambia el estado | Seleccionar un cliente, cambiar solo el combo de estado y presionar Modificar | El estado no cambia | [PENDIENTE DE VERIFICAR] | Pendiente |
| Persistencia | Hacer cambios, cerrar el programa, abrirlo y presionar Listar | Los cambios siguen ahí | [PENDIENTE DE VERIFICAR] | Pendiente |
| Sin db.properties | Renombrar `src/db.properties` y abrir el programa | Mensaje claro indicando cómo crearlo | [PENDIENTE DE VERIFICAR] | Pendiente |

## Qué se revisó y qué no

- Revisado: el conjunto compila con JDK 21; las firmas coinciden con el contrato; la regex de correo se probó con 9 casos (4 válidos, 5 inválidos); el script SQL del Integrante 1 se analizó con un parser de sintaxis MySQL (6 sentencias, sin errores) y sus columnas de `clientes` coinciden con las que usa `ClienteDAO`.
- No revisado: ejecución real del script y del programa contra MySQL, y funcionamiento de la ventana.
