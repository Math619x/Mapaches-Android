# Mapaches y compañía 🦝

Juego arcade para Android: toca los personajes que salen de sus madrigueras y evita los objetos peligrosos. Proyecto de portafolio con interfaz y dibujos propios implementados en Java, música ambiental y progreso local.

**Versión incluida:** 0.6-demo (`versionCode 6`). **Paquete:** `com.rey.pulso`.

## Características

- Tres modos: Clásico, Contrarreloj de 60 segundos y Madrigueras, con seis agujeros y hasta dos personajes simultáneos.
- Tres dificultades: Relajado, Normal y Difícil.
- Animaciones al salir y esconderse. Área de toque de tamaño máximo que incluye el personaje y el agujero.
- Una vida adicional cada 20 puntos en los modos que usan vidas; rachas y estadísticas al terminar.
- Diez mejores puntuaciones con nick por modo y dificultad.
- Nueve personajes, cinco escenarios y cinco objetos peligrosos en el catálogo. Dos opciones gratuitas de cada categoría.
- Vista previa de contenido y desbloqueos de prueba claramente identificados. **Esta versión no cobra dinero ni integra Google Play Billing.**
- Música ambiental WAV, efectos de sonido, controles de volumen y salida del juego.
- Funcionamiento local sin servidor; el manifiesto no solicita permiso de Internet.

## Tecnologías y archivos

| Elemento | Uso |
| --- | --- |
| Java 8 | Lógica, pantallas, interacción y dibujo con APIs nativas de Android |
| XML | Manifiesto e iconos vectoriales |
| Python 3.9 o posterior | Automatización de pruebas y construcción del APK |
| WAV | Música y efectos incluidos en `res/raw` |
| SharedPreferences | Ajustes, nicks, récords y desbloqueos de demostración locales |
| ECJ, AAPT2, D8, zipalign y apksigner | Compilación Java, recursos, bytecode Android, alineación y firma |

```text
README.md
.gitignore
work/
  build-pulso.py
  pulso/
    AndroidManifest.xml
    src/com/rey/pulso/   # 12 clases Java
    res/drawable/       # Iconos XML
    res/raw/            # Música y cinco efectos WAV
    tests/              # Cuatro pruebas vigentes y una histórica
```

`MainActivity` conserva el punto de entrada. `ArcadeActivity` organiza las pantallas; `ArcadeEngine` las reglas actuales; `ArcadeArt` y `ThemeArt` los dibujos. `GameOptions` define el catálogo y `ContentAccess` controla el acceso de demostración. `ScoreTable` gestiona las puntuaciones; `AmbientMusic` y `SoundEffects`, el audio. `GameEngine` y `GameArt` conservan componentes de etapas anteriores.

## Compilar en Windows

El proyecto utiliza un script directo de compilación; no incluye un proyecto Gradle. Las herramientas externas se instalan por separado y no están dentro de este repositorio.

1. Instala Python 3.9 o posterior y un **JDK 17 completo**.
2. Instala Android SDK Platform 30 y Build-Tools 35.0.0 con el [SDK Manager de Android](https://developer.android.com/tools/sdkmanager): `sdkmanager "platforms;android-30" "build-tools;35.0.0"`.
3. Descarga [Eclipse ECJ 3.26.0](https://central.sonatype.com/artifact/org.eclipse.jdt/ecj/3.26.0) y guarda el JAR como `work/toolchain/ecj.jar`, o configura `ECJ_JAR` con su ruta.
4. Abre PowerShell en la raíz del repositorio y configura las rutas de tu equipo:

```powershell
$env:JAVA_HOME = "C:\ruta\al\jdk-17"
$env:ANDROID_SDK_ROOT = "$env:LOCALAPPDATA\Android\Sdk"
python work/build-pulso.py
```

El script ejecuta las cuatro pruebas vigentes, compila, alinea y firma un APK de prueba en `outputs/Mapaches-y-compania-0.6-demo.apk`. Crea una clave local en `work/local-keys/mapaches-local.jks`, con contraseña de desarrollo `android`. Esa clave y los archivos generados están excluidos por `.gitignore`.

Si tus herramientas están en otras ubicaciones, configura `ANDROID_JAR`, `ANDROID_BUILD_TOOLS` y `ECJ_JAR` con sus rutas completas. `ANDROID_BUILD_TOOLS` debe apuntar al directorio que contiene AAPT2, zipalign y `lib/`.

Para ejecutar solamente las pruebas vigentes:

```powershell
python work/build-pulso.py --tests-only
```

Para firmar con una clave existente, configura `MAPACHES_KEYSTORE`, `MAPACHES_KEY_ALIAS`, `MAPACHES_STOREPASS` y, si difiere, `MAPACHES_KEYPASS`. Mantén esos valores y tu clave fuera del repositorio. La clave original del desarrollo no se distribuye. Un APK con una firma nueva no puede actualizar una instalación firmada con otra clave; desinstalar la aplicación elimina sus datos locales.

## Pruebas y alcance

- `ThemedGameTest`: animación y lógica de la versión temática.
- `ArcadeChangesTest`: área de toque fija y clasificación de puntuaciones.
- `ArcadeExpansionTest`: modos, vidas, rachas y reglas del motor actual.
- `ContentAccessTest`: contenido gratuito y desbloqueos de demostración.

`GameEngineTest.java` se conserva como prueba histórica de la primera versión Pulso, anterior a las transiciones de salida; no forma parte de la batería vigente y sus expectativas no corresponden al comportamiento actual. Las pruebas de lógica no sustituyen la verificación visual y de audio en un teléfono.

El manifiesto conserva Android mínimo API 23 y objetivo API 35. Este paquete prepara el código para portafolio; la publicación en una tienda requiere revisar sus requisitos, generar el formato de distribución correspondiente y, si se desean compras reales, implementar y verificar la facturación.

Los datos se guardan únicamente en el dispositivo. No hay sincronización ni restauración tras desinstalar. El personaje «Tu amigo» es una ilustración dentro del código; no se incluye la fotografía de referencia. Antes de publicar recursos basados en personas, acuerda su uso con ellas.

## Subir como proyecto independiente

Descomprime el paquete y sube **el contenido de esta carpeta**, con `README.md` en la raíz, a un repositorio nuevo llamado, por ejemplo, `mapaches-y-compania`. Incluye `.gitignore`. Instala las herramientas para compilar; no las subas al repositorio.

## Autoría y licencia

Proyecto desarrollado de forma iterativa con asistencia de IA. El repositorio documenta la implementación disponible, sus recursos y sus pruebas. No se ha elegido una licencia de distribución: el titular debe definirla y comprobar los permisos de los recursos antes de autorizar reutilización por terceros.
