from pathlib import Path
import os
import shutil
import subprocess
import sys
import zipfile

base = Path(__file__).resolve().parent
project = base / 'pulso'
toolchain = Path(os.environ.get('MAPACHES_TOOLCHAIN', base / 'toolchain'))
sdk = Path(os.environ.get('ANDROID_SDK_ROOT', os.environ.get('ANDROID_HOME', toolchain)))
android = Path(os.environ.get('ANDROID_JAR', sdk / 'platforms/android-30/android.jar'))
native = Path(os.environ.get('ANDROID_BUILD_TOOLS', sdk / 'build-tools/35.0.0'))
java_name = 'java.exe' if os.name == 'nt' else 'java'
java_home = os.environ.get('JAVA_HOME')
java = Path(java_home) / 'bin' / java_name if java_home else Path(shutil.which(java_name) or java_name)
keytool = java.with_name('keytool.exe' if os.name == 'nt' else 'keytool')
ecj = Path(os.environ.get('ECJ_JAR', toolchain / 'ecj.jar'))
exe = '.exe' if os.name == 'nt' else ''
for dependency in (java, keytool, ecj, android, native / ('aapt2' + exe),
                   native / ('zipalign' + exe), native / 'lib/d8.jar', native / 'lib/apksigner.jar'):
    if not dependency.is_file():
        raise SystemExit(f'Falta una herramienta: {dependency}. Consulta README.md.')

build = project / 'build'
for name in ('classes', 'test-classes', 'dex'):
    directory = build / name
    directory.mkdir(parents=True, exist_ok=True)
    if not directory.resolve().is_relative_to(build.resolve()):
        raise SystemExit('Directorio de compilación fuera del proyecto.')
    for generated in directory.rglob('*.class'):
        generated.unlink()
    for generated in directory.glob('*.dex'):
        generated.unlink()

def run(*args):
    result = subprocess.run([str(arg) for arg in args], check=False)
    if result.returncode:
        raise SystemExit(f'Falló {Path(args[0]).name}. Revisa el mensaje anterior.')

def compile_java(destination, sources, android_api=False):
    options = ['-classpath', android] if android_api else []
    run(java, '-jar', ecj, '-source', '1.8', '-target', '1.8', '-encoding', 'UTF-8',
        '-d', destination, *options, *sources)

source = project / 'src/com/rey/pulso'
tests = build / 'test-classes'
active_tests = ['ThemedGameTest', 'ArcadeChangesTest', 'ArcadeExpansionTest', 'ContentAccessTest']
compile_java(tests, [source / (name + '.java') for name in
    ('GameEngine', 'ScoreTable', 'GameOptions', 'ArcadeEngine', 'ContentAccess')]
    + [project / 'tests' / (name + '.java') for name in active_tests])
for name in active_tests:
    run(java, '-cp', tests, name)
if '--tests-only' in sys.argv:
    raise SystemExit(0)

compile_java(build / 'classes', sorted(project.glob('src/**/*.java')), android_api=True)
run(native / ('aapt2' + exe), 'compile', '--dir', project / 'res', '-o', build / 'compiled.zip')
run(native / ('aapt2' + exe), 'link', '--manifest', project / 'AndroidManifest.xml', '-I', android,
    '-0', 'wav', '-o', build / 'resources.apk', build / 'compiled.zip')
run(java, '-cp', native / 'lib/d8.jar', 'com.android.tools.r8.D8', '--release', '--min-api', '23',
    '--lib', android, '--output', build / 'dex', *sorted((build / 'classes').rglob('*.class')))
with zipfile.ZipFile(build / 'resources.apk') as src, zipfile.ZipFile(build / 'unsigned.apk', 'w') as dst:
    for info in src.infolist():
        dst.writestr(info, src.read(info.filename))
    for dex in sorted((build / 'dex').glob('*.dex')):
        dst.write(dex, dex.name, compress_type=zipfile.ZIP_DEFLATED)
run(native / ('zipalign' + exe), '-f', '4', build / 'unsigned.apk', build / 'aligned.apk')

custom_key = bool(os.environ.get('MAPACHES_KEYSTORE'))
key = Path(os.environ.get('MAPACHES_KEYSTORE', base / 'local-keys/mapaches-local.jks'))
alias = os.environ.get('MAPACHES_KEY_ALIAS', 'mapaches')
if custom_key and not os.environ.get('MAPACHES_STOREPASS'):
    raise SystemExit('Configura MAPACHES_STOREPASS para tu clave privada.')
if custom_key and not key.is_file():
    raise SystemExit('No existe MAPACHES_KEYSTORE. No se creará otra clave en esa ruta.')
os.environ['MAPACHES_STOREPASS'] = os.environ.get('MAPACHES_STOREPASS', 'android')
os.environ['MAPACHES_KEYPASS'] = os.environ.get('MAPACHES_KEYPASS', os.environ['MAPACHES_STOREPASS'])
if not key.exists():
    key.parent.mkdir(parents=True, exist_ok=True)
    run(keytool, '-genkeypair', '-keystore', key, '-storepass:env', 'MAPACHES_STOREPASS',
        '-keypass:env', 'MAPACHES_KEYPASS', '-alias', alias, '-keyalg', 'RSA', '-keysize', '2048',
        '-validity', '10000', '-dname', 'CN=Mapaches Local Development, O=Local, C=MX')
out = base.parent / 'outputs/Mapaches-y-compania-0.6-demo.apk'
out.parent.mkdir(parents=True, exist_ok=True)
run(java, '-jar', native / 'lib/apksigner.jar', 'sign', '--ks', key, '--ks-key-alias', alias,
    '--ks-pass', 'env:MAPACHES_STOREPASS', '--key-pass', 'env:MAPACHES_KEYPASS',
    '--out', out, build / 'aligned.apk')
run(java, '-jar', native / 'lib/apksigner.jar', 'verify', '--verbose', out)
run(native / ('zipalign' + exe), '-c', '4', out)
with zipfile.ZipFile(out) as archive:
    if archive.testzip() is not None:
        raise SystemExit('APK dañado.')
print('APK:', out, out.stat().st_size, 'bytes')
