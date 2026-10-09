# VozBarrial

Aplicación Android para participación ciudadana con autenticación y almacenamiento remoto mediante Firebase.

> **Estado actual:** aplicación Android con Jetpack Compose y Firebase Authentication, Cloud Firestore y Cloud Storage. La compilación Debug se verificó en esta configuración. La disponibilidad real de las operaciones depende de las reglas y configuración del proyecto Firebase.

## Arquitectura

El módulo Android sigue MVVM con responsabilidades separadas por capa:

- `features`: pantallas Compose y ViewModels con estado observable mediante `StateFlow`.
- `domain`: modelos y contratos (`*Gateway`) que definen lo que la aplicación necesita de cada repositorio, sin importar Firebase.
- `data`: implementaciones Firebase de los contratos de dominio.
- `di/AppContainer`: punto único de creación y compartición de clientes, repositorios y fábricas de ViewModel.
- `navigation`: coordinación de rutas y flujos entre pantallas.

El flujo esperado es `Compose → ViewModel → contrato de dominio → repositorio de datos → Firebase`. Las pantallas envían eventos y representan `UiState`; las llamadas a Firebase no se hacen desde los composables. Los estados propios del widget o de recursos Android (por ejemplo, visibilidad de un menú y ciclo de vida del mapa) pueden permanecer en Compose.

Para añadir una función, crea su modelo/contrato de dominio, implementación bajo `data`, estado y ViewModel dentro de `features/<función>`, registra las dependencias en `AppContainer` y conecta la pantalla desde `AppNavGraph`. Mantén Firebase detrás de los contratos para que la lógica se pueda sustituir o probar con implementaciones falsas.

## Tecnologías

- Kotlin
- Android Jetpack Compose y Material 3
- Gradle Kotlin DSL
- Android SDK: `compileSdk 37`, `targetSdk 35`, `minSdk 23`
- Selector de ubicación del reporte: WebView con Leaflet y teselas de OpenStreetMap
- Ubicación del dispositivo: `LocationManager` de Android con permisos de ubicación aproximada o precisa

## Estructura del proyecto

```text
app/src/main/java/com/vozbarrial/
├── data/                         Implementaciones Firebase de los repositorios
├── di/AppContainer.kt            Clientes compartidos y composición de dependencias
├── domain/                       Entidades, errores y contratos de repositorio
├── features/
│   ├── auth/                     Acceso, registro y recuperación
│   ├── dashboard/                Perfil, reportes y tienda
│   ├── map/                      Mapa y filtros de reportes
│   ├── profile/                  Estado de perfil y formulario de edición
│   ├── reports/                  Estado y operaciones de reportes
│   └── store/                    Operaciones de marcos y puntos
├── navigation/                   Rutas y coordinación de pantallas
├── MainActivity.kt               Actividad y composición raíz
└── VozBarrialApplication.kt      Ciclo de vida del contenedor
```

## Pantallas y navegación

`AppNavGraph` coordina bienvenida, autenticación, mapa, panel comunitario, perfil, marcos, tienda y creación de reportes. `NavigationViewModel` conserva la ruta y la página activa usando `SavedStateHandle`. El estado de sesión y los datos compartidos se observan desde ViewModels y se recolectan con `collectAsStateWithLifecycle`.

## Mapas y ubicación

La app solicita permisos de ubicación cuando se necesitan, permite elegir un punto en el mapa de creación y representa los reportes recibidos desde Firestore. El selector de punto usa Leaflet dentro de un WebView y teselas de OpenStreetMap; requiere conexión a Internet. Los objetos de Android y el ciclo de vida del mapa permanecen en la capa de UI.
## Datos e integración Firebase

- Firebase Authentication gestiona las cuentas; Firestore almacena perfiles (`usuarios`) y reportes (`reportes`).
- Cloud Storage guarda la evidencia de reportes y las fotos de perfil.
- Las compras de marcos actualizan puntos, inventario y marco equipado mediante una transacción de Firestore.
- `google-services.json` configura el cliente, pero no sustituye las reglas de seguridad. Antes de publicar, restringe Firestore y Storage por UID y valida operaciones sensibles desde una fuente confiable.
## Requisitos

- Android Studio con Android SDK Platform 37 y herramientas de compilación compatibles.
- JDK compatible con Android Gradle Plugin 9.1.1 (el JBR incluido con Android Studio es la opción recomendada).
- Emulador Android o dispositivo con Android 6.0 (API 23) o superior.
- Conexión a Internet para descargar dependencias de Gradle y cargar OpenStreetMap/Leaflet.

El repositorio incluye `gradlew` y `gradlew.bat` para compilar mediante el wrapper de Gradle.

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
