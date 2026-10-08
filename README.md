# VozBarrial

Aplicación Android para participación ciudadana y colaboración vecinal. Permite crear una cuenta local, navegar un mapa de reportes, preparar reportes con evidencia y ubicación, y consultar pantallas de perfil, actividad, logros y marcos.

> **Estado actual:** prototipo Android funcional en interfaz. La autenticación y el perfil usan almacenamiento local del dispositivo. No hay API, base de datos remota, envío de correos ni sincronización entre usuarios implementados todavía.

## Tecnologías

- Kotlin
- Android Jetpack Compose y Material 3
- Gradle Kotlin DSL
- Android SDK: `compileSdk 37`, `targetSdk 35`, `minSdk 23`
- Selector de ubicación del reporte: WebView con Leaflet y teselas de OpenStreetMap
- Ubicación del dispositivo: `LocationManager` de Android con permisos de ubicación aproximada o precisa

## Estructura del proyecto

```text
App-VozBarrial/
├── app/
│   ├── build.gradle.kts                 # Configuración del módulo Android y dependencias
│   └── src/main/
│       ├── AndroidManifest.xml          # Actividad principal, permisos e íconos
│       ├── java/com/vozbarrial/
│       │   └── MainActivity.kt          # Estado de navegación, cuentas locales y composición raíz
│       └── res/
│           ├── mipmap-nodpi/            # PNG del ícono de VozBarrial
│           ├── mipmap-anydpi-v26/       # Ícono adaptable para Android 8+
│           └── values/                  # Color de fondo del ícono adaptable
├── domain/
│   └── Usuario.kt                       # Espacio previsto para el modelo de usuario
├── features/
│   ├── welcome/Welcome.kt               # Bienvenida
│   ├── auth/
│   │   ├── Auth.kt                      # Inicio de sesión y acceso a registro/recuperación
│   │   ├── Register.kt                  # Registro
│   │   └── Recovery.kt                  # Cambio local de contraseña y confirmación
│   ├── map/ReportsMap.kt                # Mapa interactivo de reportes y navegación principal
│   └── dashboard/
│       ├── ReportCreate.kt              # Formulario, evidencia y ubicación del reporte
│       ├── CommunityPage.kt             # Perfil, actividad y logros
│       ├── EditProfile.kt               # Edición de datos y foto
│       ├── FramesPage.kt                # Marcos adquiridos y equipamiento
│       └── StorePage.kt                 # Catálogo y canje de marcos
├── navigation/
│   └── DashboardRoutes.kt               # Archivo reservado para rutas (la navegación vive hoy en MainActivity)
├── build.gradle.kts                     # Versiones de plugins
├── settings.gradle.kts                  # Configuración del proyecto Gradle
└── gradle.properties                    # Opciones de Gradle y Kotlin
```

Los directorios `features/`, `navigation/` y `domain/` se incluyen como fuentes Kotlin del módulo `app` desde `app/build.gradle.kts`.

## Pantallas y navegación

1. **Bienvenida** (`Welcome`): acceso al inicio de sesión.
2. **Autenticación** (`Auth`): inicio de sesión, formulario de registro y recuperación de contraseña.
3. **Registro** (`Register`): nombre, correo, contraseña, validaciones y aceptación de términos.
4. **Recuperación** (`PasswordRecovery`): solicita el correo, permite definir una nueva contraseña y muestra confirmación.
5. **Mapa** (`ReportsMap`): filtros por tipo de reporte, zoom, selección de marcadores, tarjeta de detalle y acceso a las demás áreas.
6. **Crear reporte** (`ReportCreate`): seis categorías, título, descripción, evidencia fotográfica, ubicación GPS o selección manual en el mapa y validación antes de publicar.
7. **Perfil ciudadano** (`CommunityPage`, página `profile`): nombre, nivel, puntos, estadísticas, insignias, publicaciones y acciones de cuenta.
8. **Actividad** (`CommunityPage`, página `activity`): alertas y publicaciones de actividad comunitaria.
9. **Insignias y logros** (`CommunityPage`, página `badges`): progreso de reconocimientos.
10. **Editar perfil** (`EditProfile`): nombre, teléfono y selección de imagen JPG/PNG de hasta 5 MB.
11. **Mis marcos** (`FramesPage`): marcos disponibles para equipar.
12. **Tienda** (`StorePage`): catálogo de marcos, vista previa y canje con puntos locales.

La navegación y los datos compartidos entre pantallas se coordinan actualmente mediante estado de Compose en `MainActivity`; no se usa una librería de navegación.

## Ubicación y mapas

- El formulario de reporte solicita permiso de ubicación aproximada o precisa y obtiene una ubicación reciente con los proveedores habilitados de Android. Si no hay una ubicación reciente, espera una actualización por un tiempo limitado.
- También permite elegir un punto manualmente en un mapa Leaflet dentro de un WebView. El mapa usa teselas públicas de OpenStreetMap y requiere conexión a Internet.
- El mapa principal de `ReportsMap` es una ilustración Compose con marcadores de demostración y controles de interacción. No carga calles GPS ni reportes desde un servicio en tiempo real.
- La ubicación elegida se transmite al mapa principal para ubicar visualmente el reporte dentro de la ilustración; no equivale a publicar el reporte en un mapa real compartido.

## Datos, cuentas y límites actuales

- Las cuentas se guardan en `SharedPreferences` locales (`voz_barrial_accounts`). No existe backend ni sincronización con otros dispositivos.
- La contraseña se almacena como hash SHA-256 con sal aleatoria por cuenta. Esta implementación local es para el prototipo; una aplicación pública debe delegar autenticación y credenciales a un servicio seguro.
- El cambio de contraseña actual actualiza la cuenta localmente. No envía un enlace de recuperación por correo ni verifica un token recibido.
- Los reportes de ejemplo, actividad, métricas, niveles e insignias son datos de demostración. Los reportes creados se conservan en el estado de la sesión del mapa, no en almacenamiento persistente ni en un servidor.
- La tienda modifica puntos, inventario y marco equipado en estado/almacenamiento local.
- La foto de perfil se guarda como URI local del dispositivo; no se sube a un servidor.
- Cerrar sesión vuelve a la bienvenida. El borrado de cuenta quita los datos locales asociados al usuario.

## Requisitos

- Android Studio con Android SDK Platform 37 y herramientas de compilación compatibles.
- JDK compatible con Android Gradle Plugin 9.1.1 (el JBR incluido con Android Studio es la opción recomendada).
- Emulador Android o dispositivo con Android 6.0 (API 23) o superior.
- Conexión a Internet para descargar dependencias de Gradle y cargar OpenStreetMap/Leaflet.

El repositorio no incluye `gradlew`/`gradlew.bat`. Se puede abrir la carpeta del proyecto en Android Studio y dejar que Gradle sincronice usando la instalación de Gradle disponible en el entorno.

## Ejecutar en Android Studio

1. Abre `App-VozBarrial` como proyecto en Android Studio.
2. Espera a que termine la sincronización de Gradle.
3. Inicia un emulador o conecta un dispositivo Android.
4. Selecciona la configuración `app` y pulsa **Run**.
5. Para probar GPS en el emulador, establece una ubicación desde **Extended controls → Location** y concede el permiso cuando la aplicación lo solicite.

Desde una terminal que tenga Gradle instalado y configurado:

```powershell
gradle :app:assembleDebug
gradle :app:installDebug
```

El APK de depuración se genera normalmente en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Permisos Android

El manifiesto solicita:

- `INTERNET`: carga de mapas, teselas y recursos de Leaflet.
- `ACCESS_COARSE_LOCATION`: ubicación aproximada.
- `ACCESS_FINE_LOCATION`: ubicación precisa.

La ubicación se solicita en tiempo de ejecución desde el formulario de creación del reporte.

## Ícono de la aplicación

El launcher utiliza el logo PNG compartido en `app/src/main/res/mipmap-nodpi/`. `AndroidManifest.xml` declara `logo_vozbarrial` como ícono y `logo_vozbarrial_round` como variante redonda; Android 8 o superior usa las definiciones adaptables de `mipmap-anydpi-v26/`.

## Configuración técnica

- Proyecto Gradle: `VozBarrial`
- Módulo Android: `:app`
- `applicationId` y `namespace`: `com.vozbarrial`
- Versión: `1.0` (`versionCode 1`)
- Plugins declarados en la raíz: Android application `9.1.1` y Kotlin Compose `2.3.20`
- Dependencias principales: `androidx.core:core-ktx`, `androidx.activity:activity-compose`, Compose BOM, Compose UI, Material 3 e iconos extendidos de Material

## Próximos pasos sugeridos

- Crear un backend y persistencia remota para cuentas, reportes, comentarios e imágenes.
- Reemplazar la autenticación local por un proveedor seguro y recuperación de contraseña por correo con token.
- Conectar el mapa principal a cartografía real y cargar reportes desde la API.
- Persistir reportes y puntos, y definir reglas para su verificación y recompensas.
- Completar pruebas automatizadas y configurar un flujo de compilación para versiones de distribución.
