# mynotes (IotasLife)

Aplicacion Android moderna de registro de bitacora laboral, gestion de tickets y pendientes organizada por empresas/organizaciones, inspirada en la elegancia visual de Notion, la agilidad de Iotas y el diseno dinamico Material You (Material 3).

---

## Caracteristicas Principales

### Gestion de Organizaciones (Empresas)
- Selector y creador dinamico de organizaciones/empresas con soporte de etiquetas de categoria (CORP, DEV, OPS, SYS, FIN).
- Todo el contenido (bitacora y pendientes) esta completamente aislado y clasificado por la organizacion activa.
- Mensajes emergentes (Toast) informativos para cada accion.

### Bitacora de Trabajo Diaria (Tickets de Documentacion)
- Disenada para documentar el trabajo diario de manera profesional tipo ticket tecnico.
- **Campos estructurados**:
  - Codigo de Ticket (ej. TIK-101)
  - Fecha de registro y selector de fecha
  - Estado dinamico: En Progreso, Completado, Bloqueado, En Revision (con pastillas de color pastel estilo Notion)
  - **Lo que se hizo ese dia** (descripcion detallada del trabajo realizado)
  - **Acciones realizadas** (pasos concretos, comandos, scripts aplicados)
  - **Observaciones** (alertas, incidencias, notas tecnicas)
- **Evidencias Fotograficas Privadas**:
  - Permite adjuntar multiples imagenes como evidencia fotografica de las actividades.
  - Almacenamiento seguro y privado en `context.filesDir/evidence_images/`.
  - **Sin duplicados en galeria**: Los archivos no son indexados por MediaScanner, por lo que nunca apareceran duplicados en la galeria publica del dispositivo.
  - Visor modal de evidencias a pantalla completa y previsualizacion con miniaturas.
- **Exportacion a Excel**:
  - Exportacion directa de la bitacora de la empresa a formato Excel (`.csv` delimitado por punto y coma `;` con BOM UTF-8 `\uFEFF`).
  - Compatible de forma nativa con Microsoft Excel, LibreOffice Calc y Google Sheets sin problemas de codificacion de caracteres.
  - Comparticion nativa mediante selector de Android con FileProvider.
- **Busqueda y Organizacion Flexible**:
  - Filtro rapido por chip **"Hoy"** para acceder inmediatamente a las notas del dia.
  - Busqueda en tiempo real por fecha (ej. "hoy", "04 nov"), codigo de ticket, titulo o descripcion/observaciones.
  - Filtro por estado del ticket (Todos, En Progreso, Completado, etc.).
- **Operaciones completas (CRUD)**:
  - Guardado / Creacion de nuevos registros
  - Edicion en caliente
  - Eliminacion segura con dialogo de confirmacion y notificaciones Toast

### Apartado de Pendientes (To-Dos)
- Lista de compromisos y tareas rapidas por organizacion.
- Casilla de verificacion estilo Notion (checkbox con animacion visual y tachado de tareas finalizadas).
- Filtros por: Todos, Pendientes y Finalizados con conteo dinamico.
- Prioridades configurables: Baja, Media, Alta.
- CRUD completo (crear, editar, toggle completado, eliminar) con notificaciones Toast.

---

## Diseno y Experiencia de Usuario

- **Material You (Dynamic Colors) & Esquemorfismo Evolucionado**:
  - Combinacion de elementos esquemorficos clasicos de un bloc de notas fisico (anillas metalicas en espiral, borde perforado de desgarre, papel pautado con lineas y margen vertical, tarjetas de nota adhesiva post-it con cinta adhesiva) con la armonia moderna de Dynamic Colors (Material You) en Android 12+ (SDK 31+) y respaldo en paleta Notion.
- **Pendientes Vinculados a la Bitacora**:
  - Creacion directa de actividades pendientes por completar o revisar desde el editor de cada ticket de bitacora.
  - Asistente de un clic para copiar texto de "Acciones realizadas" u "Observaciones" hacia un nuevo pendiente.
  - Boton de accion rapida `+ Pendiente` en las tarjetas de bitacora del listado principal.
  - Indicadores y sellos de ticket (`[TIK-XXXX]`) en la pantalla de pendientes para trazabilidad total.
- **Retroalimentacion Toast**: Confirmaciones inmediatas para operaciones de guardado, actualizacion, eliminacion y exportacion.
- **Clean Architecture & Clean Code**:
  - `domain`: Modelos inmutables (`Organization`, `BitacoraEntry`, `TaskItem`) y contratos de repositorio.
  - `data`: Persistencia local con Room 2.6.1 + KSP, TypeConverters y repositorios.
  - `presentation`: Componentes desacoplados en Jetpack Compose, ViewModels reactivos con StateFlow.
- **Internacionalizacion y Buenas Practicas**:
  - El 100% de textos, cadenas y mensajes de Toast se encuentran centralizados en `res/values/strings.xml`.
  - Politica estricta de Cero Emojis (Zero Emojis Policy) en codigo fuente, UI y scripts.

---

## Lanzador Automatico ADB (`launch_app.sh`)

Se incluye un script ejecutable en la raiz del proyecto para compilar, instalar y lanzar la aplicacion automaticamente en cualquier dispositivo fisico o emulador conectado por ADB:

```bash
./launch_app.sh
```

El script detecta automaticamente:
1. `JAVA_HOME` (soporte para JDK 21 / 17).
2. Binario `adb` en PATH o Android SDK.
3. Dispositivos/emuladores conectados.
4. Compila el APK Debug (`./gradlew assembleDebug`).
5. Instala mediante `adb install -r`.
6. Lanza la actividad principal (`MainActivity`).

---

## Pruebas Automatizadas

Pruebas instrumentadas de ciclo de vida completo verificadas en emulador Android (`BitacoraCrudInstrumentedTest`):
- `testCompleteBitacoraLifecycle_Save_Edit_Delete`: Validacion de persistencia, consulta, edicion y eliminacion de bitacoras.
- `testTasksLifecycle_Create_ToggleCompleted_Delete`: Creacion, alternado de estado completado/pendiente y eliminacion de tareas.
- `testBitacoraWithImageEvidence`: Almacenamiento y recuperacion integra de evidencias fotograficas en Room.

Para ejecutar las pruebas:
```bash
./gradlew connectedAndroidTest
```

---

## Historial de Cambios

Para consultar el registro detallado de versiones y modificaciones arquitectonicas, revisa el archivo [CHANGELOG.md](CHANGELOG.md).
