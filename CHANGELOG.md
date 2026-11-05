# Changelog

Todas las modificaciones notables de este proyecto estan documentadas en este archivo.
El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y cumple con la especificacion de Commits Convencionales y Politica Zero Emojis.

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
