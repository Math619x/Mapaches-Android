# Validación del paquete de portafolio

Fecha: 21 de noviembre de 2026.

Se compiló esta copia con Python 3.13, JDK 17, ECJ 3.26.0, Android SDK Platform 30 y Build-Tools 35. Se generó un APK y se verificaron su integridad ZIP, alineación y firma.

| Prueba vigente | Comprobaciones aprobadas |
| --- | ---: |
| ThemedGameTest | 46 146 |
| ArcadeChangesTest | 91 |
| ArcadeExpansionTest | 50 084 |
| ContentAccessTest | 45 |

El compilador emitió siete advertencias por APIs Android obsoletas; no hubo errores de compilación. Las clases, el manifiesto y los recursos distribuidos coinciden byte por byte con los archivos del proyecto original. El script de compilación se adaptó para configurar las herramientas por variables de entorno y generar una firma local separada.

No se ejecutaron pruebas de interfaz ni audio en un dispositivo durante esta exportación. La prueba histórica GameEngineTest no pertenece a la batería vigente, como se explica en el README. El APK generado, las herramientas y las claves locales no se incluyen en el ZIP de código fuente.
