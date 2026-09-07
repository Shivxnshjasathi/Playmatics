import java.io.File
import java.util.zip.ZipFile

val jars = File(System.getProperty("user.home"), ".gradle/caches/modules-2/files-2.1/io.github.jan-tennert.supabase").walkTopDown().filter { it.extension == "jar" && "realtime-kt" in it.name }.toList()
for (jar in jars) {
    println(jar.name)
    val zf = ZipFile(jar)
    for (entry in zf.entries()) {
        if ("presence" in entry.name.toLowerCase() || "broadcast" in entry.name.toLowerCase()) {
            println(" - " + entry.name)
        }
    }
}
