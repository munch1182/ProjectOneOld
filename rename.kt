import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException


/**
 * 使用:
 *
 * 更改newProjectName的名称
 * 然后执行:
 * kotlinc rename.kt -include-runtime -d rename.jar ; java -jar rename.jar ; del rename.jar
 */
private const val newProjectName = "NewName"

//<editor-fold desc="code">
private val newPkgName = "com.munch.project.${newProjectName.lowercase()}"

private const val oldPkgName = "com.munch.template.android"

private const val cancelDir = "./rename"

fun main() {
    val res = updateSettingGradle() && updateApp() && updateManifest() && updateAppName()
    if (res) clear()
}

fun clear() {
    try {
        File(cancelDir).deleteRecursively()
        println("now, you can sync file and project.")
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun updateAppName(): Boolean {
    val strings = File("./app/src/main/res/values/strings.xml")
    val stringsBak = File(cancelDir, "strings.xml.bak")
    return strings.bak(stringsBak) && stringsBak.copyReplace(strings) { line ->
        line.replace("ProjectOne", newProjectName)
    }
}

fun updateManifest(): Boolean {
    val manifest = File("./app/src/main/AndroidManifest.xml")
    val manifestBak = File(cancelDir, "AndroidManifest.xml.bak")
    return manifest.bak(manifestBak) && manifestBak.copyReplace(manifest)
}

private fun updateApp(): Boolean {
    val packJava = File("./app/src/main/java")
    val mainActivityPath = "${oldPkgName.replace(".", "/")}/MainActivity.kt"
    val mainActivityBakPath = "${newPkgName.replace(".", "/")}/MainActivity.kt.bak"

    val mainActivity = File(packJava, mainActivityPath)
    val mainActivityBak = File(cancelDir, mainActivityBakPath)

    if (!mainActivity.bak(mainActivityBak)) {
        return false
    }

    val newPkg = File(packJava, newPkgName.replace(".", "/"))

    try {
        val res = File(packJava, "/com").deleteRecursively() && newPkg.mkdirs()
        println("del $packJava and create $newPkg: $res")
    } catch (e: IOException) {
        println("$packJava delete fail")
        return false
    }

    val newMainActivity = File(packJava, mainActivityBakPath.replace(".bak", ""))
    return mainActivityBak.copyReplace(newMainActivity)
}

private fun updateSettingGradle(): Boolean {
    val settingGradle = File("./settings.gradle")
    val settingGradleBak = File(cancelDir, "settings.gradle.bak")
    return settingGradle.bak(settingGradleBak) &&
            settingGradleBak.copyReplace(settingGradle) { line ->
                if (line.startsWith("rootProject.name")) {
                    "rootProject.name = \"${newProjectName}\""
                } else {
                    line
                }
            }
}

private fun File.bak(dest: File): Boolean {
    try {
        dest.delete()
        this.copyTo(dest, true)
    } catch (e: IOException) {
        println("${this.path} backup error: ${e.message}")
        return false
    }
    return true
}

private fun File.copyReplace(
    dest: File,
    replace: ((String) -> String) = { it.replace(oldPkgName, newPkgName) }
): Boolean {
    try {
        FileReader(this).use { reader ->
            val separator = System.getProperty("line.separator")
            FileWriter(dest).use { writer ->
                writer.write("")
                writer.flush()
                reader.readLines().forEach { line ->
                    writer.write(replace.invoke(line))
                    writer.write(separator)
                }
                println("$dest update complete.")
            }
        }
    } catch (e: IOException) {
        println("$dest update fail: ${e.message}")
        return false
    }
    return true
}
//</editor-fold>