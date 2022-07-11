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
    return strings.update { line ->
        line.replace("ProjectOne", newProjectName)
    }
}

fun updateManifest(): Boolean {
    val manifest = File("./app/src/main/AndroidManifest.xml")
    return manifest.update()
}

private fun updateApp(): Boolean {
    val packJava = File("./app/src/main/java")

    val oldPkgPath = oldPkgName.replace(".", "/")

    val mainActivityPath = "$oldPkgPath/MainActivity.kt"
    val mainActivity = File(packJava, mainActivityPath)
    var update = mainActivity.update()

    val androidTest = File("./app/src/androidTest/java/${oldPkgPath}/ExampleInstrumentedTest.kt")
    update = update && androidTest.update()
    val test = File("./app/src/test/java/${oldPkgPath}/ExampleUnitTest.kt")
    return update && test.update()
}

private fun updateSettingGradle(): Boolean {
    val settingGradle = File("./settings.gradle")
    return settingGradle.update { line ->
        if (line.startsWith("rootProject.name")) {
            "rootProject.name = \"${newProjectName}\""
        } else {
            line
        }
    }
}

private fun File.update(
    replace: ((String) -> String) = { it.replace(oldPkgName, newPkgName) }
): Boolean {
    try {
        val oldPkgPath = oldPkgName.replace(".", "\\")
        val newPkgPath = newPkgName.replace(".", "\\")
        val bakFile = File(cancelDir, this.path)
        if (!this.bak(bakFile)) {
            return false
        }
        var newFile = this
        val filePath = path
        if (filePath.contains(oldPkgPath)) {
            val dir = File(filePath.subSequence(0, filePath.indexOf(oldPkgPath)).toString())
            val del = dir.listFiles()?.firstOrNull()?.deleteRecursively() ?: false
            if (!del) {
                return false
            }
            newFile = File(filePath.replace(oldPkgPath, newPkgPath))
        }
        newFile.parentFile?.mkdirs()
        newFile.createNewFile()
        return bakFile.copyReplace(newFile, replace)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return false
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