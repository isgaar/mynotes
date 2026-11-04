# IotasLife 📱✨

Aplicación Android moderna de registro de bitácora laboral, gestión de tickets y pendientes organizada por empresas/organizaciones, inspirada en la elegancia visual de **Notion** y la agilidad de toma de notas de **Iotas**.

---

## 🌟 Características Principales

### 🏢 Gestión de Organizaciones (Empresas)
- Selector y creador dinámico de organizaciones/empresas con soporte de emojis personalizados.
- Todo el contenido (bitácora y pendientes) está completamente aislado y clasificado por organización activa.

### 📝 Bitácora de Trabajo Diaria (Tickets de Documentación)
- Diseñada para documentar el trabajo diario de manera profesional tipo ticket técnico.
- **Campos estructurados**:
  - Código de Ticket (ej. `TIK-101`)
  - Fecha de registro
  - Estado dinámico: *En Progreso*, *Completado*, *Bloqueado*, *En Revisión* (con pastillas de color pastel estilo Notion)
  - **Lo que se hizo ese día**
  - **Acciones realizadas** (pasos concretos, comandos, scripts)
  - **Observaciones** (alertas, incidencias, notas técnicas)
- **Operaciones completas (CRUD)**:
  - Guardado / Creación de nuevos registros
  - Búsqueda en tiempo real por título, ticket o contenido
  - Filtro interactivo por estado
  - Edición en caliente
  - Eliminación segura con diálogo de confirmación

### ✅ Apartado de Pendientes (To-Dos)
- Lista de compromisos y tareas rápidas por organización.
- Casilla de verificación estilo Notion (checkbox con animación visual y tachado de tareas finalizadas).
- Filtros por: *Todos*, *Pendientes* y *Finalizados* con conteo dinámico.
- Prioridades configurables: *Baja*, *Media*, *Alta*.
- CRUD completo (crear, editar, toggle completado, eliminar).

---

## 🛠️ Stack Tecnológico & Buenas Prácticas

- **Kotlin 2.0.21** con **Jetpack Compose** & **Material 3**.
- **Clean Architecture & Clean Code**:
  - `domain`: Modelos inmutables y contratos de repositorios sin dependencias de frameworks.
  - `data`: Persistencia local con **Room 2.6.1** y **KSP**, mapeadores de entidad y repositorios.
  - `presentation`: Componentes desacoplados, ViewModels con `StateFlow`, `Scaffold`, `NavigationBar`.
- **Internacionalización & Strings en XML**:
  - Cumplimiento de buenas prácticas Android: todos los textos, etiquetas y mensajes de retroalimentación se encuentran centralizados en `res/values/strings.xml`.
- **Pruebas Automatizadas en Emulador**:
  - Pruebas instrumentadas completas (`BitacoraCrudInstrumentedTest`) validadas al 100% en emulador de Android.

---

## 🚀 Compilación y Ejecución

### Compilar APK Debug
```bash
./gradlew assembleDebug
```
El instalable se genera en: `app/build/outputs/apk/debug/app-debug.apk`

### Ejecutar Pruebas Instrumentadas
```bash
./gradlew connectedAndroidTest
```
