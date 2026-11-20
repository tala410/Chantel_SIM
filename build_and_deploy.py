import os
import sys
import shutil
import subprocess
import zipfile
import time

BUILD_TOOLS = "/Users/talantdzanbekov/Library/Android/sdk/build-tools/35.0.0"
PLATFORM_JAR = "/Users/talantdzanbekov/Library/Android/sdk/platforms/android-31/android.jar"
AAPT2 = os.path.join(BUILD_TOOLS, "aapt2")
D8 = os.path.join(BUILD_TOOLS, "d8")
APKSIGNER = os.path.join(BUILD_TOOLS, "apksigner")

ROOT = os.path.abspath(os.path.dirname(__file__))
APP_DIR = os.path.join(ROOT, "app")
GEN_DIR = os.path.join(ROOT, "gen")
BIN_DIR = os.path.join(ROOT, "bin")
DEX_DIR = os.path.join(ROOT, "dex_out")
LIBS_DIR = os.path.join(ROOT, "libs")
KEYSTORE_DIR = os.path.join(ROOT, "keystore")

KEY_PK8 = os.path.join(KEYSTORE_DIR, "aosp_platform.pk8")
CERT_PEM = os.path.join(KEYSTORE_DIR, "aosp_platform.x509.pem")
ZXING_JAR = os.path.join(LIBS_DIR, "zxing_core.jar")


def run_cmd(cmd):
    print(">>", cmd)
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print("STDOUT:", res.stdout)
        print("STDERR:", res.stderr)
        raise RuntimeError(f"Command failed with code {res.returncode}")
    return res.stdout


def build_apk():
    print("=== [1/6] Cleaning build artifacts ===")
    shutil.rmtree(GEN_DIR, ignore_errors=True)
    shutil.rmtree(BIN_DIR, ignore_errors=True)
    shutil.rmtree(DEX_DIR, ignore_errors=True)
    os.makedirs(GEN_DIR, exist_ok=True)
    os.makedirs(BIN_DIR, exist_ok=True)
    os.makedirs(DEX_DIR, exist_ok=True)

    print("=== [2/6] Compiling resources with AAPT2 ===")
    compiled_res = os.path.join(ROOT, "compiled_res.zip")
    if os.path.exists(compiled_res):
        os.remove(compiled_res)
    run_cmd(f'"{AAPT2}" compile --dir "{APP_DIR}/res" -o "{compiled_res}"')

    print("=== [3/6] Linking AndroidManifest and resources ===")
    base_apk = os.path.join(ROOT, "base.apk")
    if os.path.exists(base_apk):
        os.remove(base_apk)
    run_cmd(
        f'"{AAPT2}" link -I "{PLATFORM_JAR}" "{compiled_res}" '
        f'--manifest "{APP_DIR}/AndroidManifest.xml" -o "{base_apk}" --java "{GEN_DIR}"'
    )

    print("=== [4/6] Compiling Java source files ===")
    src_dir = os.path.join(APP_DIR, "src", "online", "chantel", "sim")
    java_files = [os.path.join(src_dir, f) for f in os.listdir(src_dir) if f.endswith(".java")]
    r_java = os.path.join(GEN_DIR, "online", "chantel", "sim", "R.java")
    if os.path.exists(r_java):
        java_files.append(r_java)

    cp = f"{PLATFORM_JAR}:{ZXING_JAR}:{GEN_DIR}"
    java_files_str = " ".join(f'"{f}"' for f in java_files)
    run_cmd(f'javac -source 8 -target 8 -cp "{cp}" -d "{BIN_DIR}" {java_files_str}')

    print("=== [5/6] Dexing bytecode with D8 ===")
    app_jar = os.path.join(ROOT, "app-classes.jar")
    if os.path.exists(app_jar):
        os.remove(app_jar)
    run_cmd(f'jar cf "{app_jar}" -C "{BIN_DIR}" .')

    run_cmd(f'"{D8}" --lib "{PLATFORM_JAR}" --output "{DEX_DIR}" "{app_jar}" "{ZXING_JAR}"')

    unaligned_apk = os.path.join(ROOT, "ChantelSIM_unaligned.apk")
    shutil.copyfile(base_apk, unaligned_apk)
    with zipfile.ZipFile(unaligned_apk, "a") as z:
        for f in os.listdir(DEX_DIR):
            if f.endswith(".dex"):
                z.write(os.path.join(DEX_DIR, f), f)

    print("=== [6/6] Signing APK with AOSP Platform Key ===")
    signed_apk = os.path.join(ROOT, "ChantelSIM_signed.apk")
    if os.path.exists(signed_apk):
        os.remove(signed_apk)
    run_cmd(f'"{APKSIGNER}" sign --key "{KEY_PK8}" --cert "{CERT_PEM}" --out "{signed_apk}" "{unaligned_apk}"')

    size_kb = os.path.getsize(signed_apk) / 1024.0
    print(f"\nSUCCESS! Built signed APK: {signed_apk} ({size_kb:.1f} KB)")
    return signed_apk


if __name__ == "__main__":
    apk = build_apk()
