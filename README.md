# mynotes (IotasLife) 📱✨

Aplicación Android moderna de registro de bitácora laboral, gestión de tickets y pendientes organizada por empresas/organizaciones, inspirada en la elegancia visual de **Notion**, la agilidad de **Iotas** y el diseño dinámico **Material You (Material 3)**.

---

## 🌟 Características Principales

### 🏢 Gestión de Organizaciones (Empresas)
- Selector y creador dinámico de organizaciones/empresas con soporte de emojis personalizados.
- Todo el contenido (bitácora y pendientes) está completamente aislado y clasificado por la organización activa.
- Mensajes emergentes (**Toast**) informativos para cada acción.

### 📝 Bitácora de Trabajo Diaria (Tickets de Documentación)
- Diseñada para documentar el trabajo diario de manera profesional tipo ticket técnico.
- **Campos estructurados**:
  - Código de Ticket (ej. `TIK-101`)
  - Fecha de registro y selector de fecha
  - Estado dinámico: *En Progreso*, *Completado*, *Bloqueado*, *En Revisión* (con pastillas de color pastel estilo Notion)
  - **Lo que se hizo ese día** (descripción detallada del trabajo realizado)
  - **Acciones realizadas** (pasos concretos, comandos, scripts aplicados)
  - **Observaciones** (alertas, incidencias, notas técnicas)
- **📸 Evidencias Fotográficas Privadas**:
  - Permite adjuntar múltiples imágenes como evidencia fotográfica de las actividades.
  - Almacenamiento seguro y privado en `context.filesDir/evidence_images/`.
  - **Sin duplicados en galería**: Los archivos no son indexados por MediaScanner, por lo que nunca aparecerán duplicados en la galería pública del dispositivo.
  - Visor modal de evidencias a pantalla completa y previsualización con miniaturas.
- **📊 Exportación a Excel**:
  - Exportación directa de la bitácora de la empresa a formato Excel (`.csv` delimitado por punto y coma `;` con **BOM UTF-8 `\uFEFF`**).
  - Compatible de forma nativa con Microsoft Excel, LibreOffice Calc y Google Sheets sin problemas de codificación de caracteres.
  - Compartición nativa mediante selector de Android con `FileProvider`.
- **🔍 Búsqueda y Organización Flexible**:
  - Filtro rápido por chip **"Hoy"** para acceder inmediatamente a las notas del día.
  - Búsqueda en tiempo real por fecha (ej. "hoy", "04 nov"), código de ticket, título o descripción/observaciones.
  - Filtro por estado del ticket (*Todos*, *En Progreso*, *Completado*, etc.).
- **Operaciones completas (CRUD)**:
  - Guardado / Creación de nuevos registros
  - Edición en caliente
  - Eliminación segura con diálogo de confirmación y notificaciones Toast

### ✅ Apartado de Pendientes (To-Dos)
- Lista de compromisos y tareas rápidas por organización.
- Casilla de verificación estilo Notion (checkbox con animación visual y tachado de tareas finalizadas).
- Filtros por: *Todos*, *Pendientes* y *Finalizados* con conteo dinámico.
- Prioridades configurables: *Baja*, *Media*, *Alta*.
- CRUD completo (crear, editar, toggle completado, eliminar) con notificaciones **Toast**.

---

## 🎨 Diseño y Experiencia de Usuario

- **Material You (Dynamic Colors)**: Compatible con temas dinámicos en Android 12+ (SDK 31+) adaptados a la paleta del sistema del usuario, con degradados y colores Notion de respaldo en versiones previas.
- **Retroalimentación Toast**: Confirmaciones inmediatas para operaciones de guardado, actualización, eliminación y exportación.
- **Clean Architecture & Clean Code**:
  - `domain`: Modelos inmutables (`Organization`, `BitacoraEntry`, `TaskItem`) y contratos de repositorio.
  - `data`: Persistencia local con **Room 2.6.1** + **KSP**, TypeConverters y repositorios.
  - `presentation`: Componentes desacoplados en Jetpack Compose, ViewModels reactivos con `StateFlow`.
- **Internacionalización y Buenas Prácticas**:
  - El 100% de textos, cadenas y mensajes de Toast se encuentran centralizados en `res/values/strings.xml`.

---

## 🚀 Lanzador Automático ADB (`launch_app.sh`)

Se incluye un script ejecutable en la raíz del proyecto para compilar, instalar y lanzar la aplicación automáticamente en cualquier dispositivo físico o emulador conectado por ADB:

```bash
./launch_app.sh
```

El script detecta automáticamente:
1. `JAVA_HOME` (soporte para JDK 21 / 17).
2. Binario `adb` en PATH o Android SDK.
3. Dispositivos/emuladores conectados.
4. Compila el APK Debug (`./gradlew assembleDebug`).
5. Instala mediante `adb install -r`.
6. Lanza la actividad principal (`MainActivity`).

---

## 🧪 Pruebas Automatizadas

Pruebas instrumentadas de ciclo de vida completo verificadas en emulador Android (`BitacoraCrudInstrumentedTest`):
- `testCompleteBitacoraLifecycle_Save_Edit_Delete`: Validación de persistencia, consulta, edición y eliminación de bitácoras.
- `testTasksLifecycle_Create_ToggleCompleted_Delete`: Creación, alternado de estado completado/pendiente y eliminación de tareas.
- `testBitacoraWithImageEvidence`: Almacenamiento y recuperación íntegra de evidencias fotográficas en Room.

Para ejecutar las pruebas:
```bash
./gradlew connectedAndroidTest
```
