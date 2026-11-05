# Changelog

Todas las modificaciones notables de este proyecto estan documentadas en este archivo.
El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y cumple con la especificacion de Commits Convencionales y Politica Zero Emojis.

## [1.1.0] - 2026-11-04

### Anadido
- **Evolucion Esquemorfica con Material You (Bloc de Notas Tactil):**
  - Componente de anillas de libreta / espiral metalico en espiral `NotebookSpiralHeader` con gradientes metalicos y orificios troquelados con sombra interior tridimensional adaptables a la paleta dinamica del sistema.
  - Linea de desgarre perforada `PerforatedTearEdge` que evoca el borde troquelado de un bloc de notas fisico.
  - Hoja de papel rayada / pautada `NotepadPaperField` y modificador `notepadRuledPaper` con lineas horizontales sutiles y linea de margen vertical clasica en rojo/acento del tema.
  - Tarjeta de estilo nota adhesiva / post-it `StickyNoteItem` con tira de cinta adhesiva translucida superior para actividades y notas de revision.
  - Contenedor de hoja de libreta `NotepadSheetCard` con relieve tactil, sombra multinivel y bordes con bisel.
- **Creacion y Gestion de Pendientes Directos desde la Bitacora:**
  - Seccion dedicada `TicketTasksSection` integrada en el editor de bitacora para registrar tareas o actividades faltantes de revision o ejecucion.
  - Asistente inteligente con chips de sugerencia para importar pendientes directamente desde los campos "Acciones realizadas" u "Observaciones" con un solo toque.
  - Selector rapido de prioridad y boton de adicion directa que persiste las tareas en Room vinculadas mediante el identificador de ticket.
  - Lista de verificacion interactiva dentro de la misma bitacora con casillas de comprobacion para marcar avance sin salir del editor.
  - Boton de accion rapida `+ Pendiente` en cada tarjeta de bitacora del listado principal con dialogo emergente `QuickAddTicketTaskDialog`.
  - Indicador visual tipo post-it en la tarjeta del ticket con conteo de actividades pendientes y lista desplegable.
  - Etiqueta / sello de ticket vinculado (ej. `[TIK-9546]`) en la pantalla general de Pendientes para trazabilidad bidireccional.
- **Seguridad e Integridad de Datos en Dispositivo Fisico:**
  - Verificacion completa del dispositivo conectado via ADB y respaldo local seguro de base de datos SQLite y fotografias de evidencia.
  - Estrategia de vinculacion no destructiva en Room sin modificaciones de esquema, garantizando la preservacion al 100% de la informacion previamente guardada por el usuario.

### Refactorizado
- Reorganizacion modular de las vistas de bitacora y pendientes en componentes especializados (`NotepadVisuals.kt`, `NotepadPaperField.kt`, `TicketTasksSection.kt`, `TicketTaskHelper.kt`).
- Integracion de `tasksViewModel` en `BitacoraEditorScreen` y `BitacoraListScreen` para reactividad bidireccional.
- Cumplimiento de cero advertencias de compilacion (0 compiler warnings) e iconos `AutoMirrored` para compatibilidad RTL.

## [1.0.0] - 2026-11-04

### Anadido
- **Gestion de Organizaciones (Workspaces):**
  - Entidad `Organization` y persistencia en Room con eliminacion en cascada (`ForeignKey.CASCADE`).
  - Selector y creador de organizaciones con etiquetas de categoria tecnica (CORP, DEV, OPS, SYS, FIN, APP).
  - Aislamiento completo de bitacora y tareas por organizacion activa.
- **Bitacora de Trabajo Diaria (Tickets de Documentacion):**
  - Campos estructurados para codigo de ticket, fecha epoch, estado del ciclo de vida (`BitacoraStatus`), trabajo realizado, acciones tecnicas realizadas y observaciones.
  - Busqueda reactiva multi-campo en tiempo real (titulo, codigo, descripcion, acciones, observaciones y fecha).
  - Chip de filtro rapido "Hoy" para acceder inmediatamente a los registros de la fecha actual.
  - Filtro interactivo por pastillas de estado (Todos, En Progreso, Completado, Bloqueado, En Revision).
- **Evidencias Fotograficas Privadas (Zero Gallery Duplication):**
  - Integracion de Photo Picker moderno de Android (`PickMultipleVisualMedia`).
  - Almacenamiento seguro y privado en `context.filesDir/evidence_images/` sin indexacion por MediaScanner (evita duplicados en la galeria publica).
  - Componentes de previsualizacion en miniatura y visor modal a pantalla completa (`EvidenceThumbnail`, `FullImageDialog`).
- **Exportacion a Excel Nativa:**
  - Generador `ExcelExporter` que produce archivos `.csv` delimitados por punto y coma (`;`) con Byte Order Mark (BOM UTF-8 `\uFEFF`), compatible de forma directa con Microsoft Excel, Google Sheets y LibreOffice Calc.
  - Comparticion nativa mediante Android Sharesheet con `FileProvider` y `ACTION_SEND`.
- **Modulo de Pendientes (To-Dos):**
  - Casillas de verificacion animadas estilo Notion con tachado de texto.
  - Niveles de prioridad configurable (Baja, Media, Alta).
  - Pestanas de filtrado por Todos, Pendientes y Finalizados con conteo reactivo.
- **Diseno y Experiencia de Usuario:**
  - Soporte de Dynamic Color (Material You) para Android 12+ (API 31+) con respaldo en paleta Notion.
  - Notificaciones emergentes (Toast) para eventos de guardado, edicion, borrado y exportacion.
  - Internacionalizacion: 100% de textos y mensajes centralizados en `res/values/strings.xml`.
- **Despliegue y Pruebas Automatizadas:**
  - Script ejecutable `launch_app.sh` con autolocalizacion de JDK 21, binario `adb`, deteccion de dispositivos, compilacion Gradle, instalacion y arranque.
  - Suite de pruebas instrumentadas `BitacoraCrudInstrumentedTest` con 3/3 pruebas aprobadas en emulador AVD y hardware fisico.

### Refactorizado
- **Cumplimiento de Estandares Globales de Desarrollo:**
  - Aplicacion estricta de la politica Zero Emojis en el 100% del repositorio (codigo, UI, comentarios, scripts y documentacion).
  - Adicion de cabeceras canonicas de 3 lineas con descripcion tecnica (>= 100 caracteres) y atribucion `All Rights Reserved isgaar` en todos los archivos.
  - Reemplazo de `.values()` por `.entries` de Kotlin 2.0+ para evitar asignaciones de arrays en heap.
  - Migracion a iconos vectoriales bidireccionales `AutoMirrored` para compatibilidad RTL.
  - Resolucion total de advertencias de compilacion (0 compiler warnings).

