package org.codejive.kodesta.core.nodejs

import org.codejive.kodesta.core.data.jsonIo
import org.codejive.kodesta.core.data.objectFromPath
import org.codejive.kodesta.core.data.objectToPath
import java.nio.file.Path

fun mergePackageJson(targetPath: Path, sourcePath: Path) {
    val sourcePackageJson = jsonIo.objectFromPath(sourcePath)
    val targetPackageJson = jsonIo.objectFromPath(targetPath).toMutableMap()
    sourcePackageJson.entries.forEach { e ->
        val oldval = targetPackageJson[e.key]
        if (oldval is Map<*,*>) {
            val newval = mutableMapOf<String, Any?>()
            newval.putAll(oldval as Map<String, Any?>)
            newval.putAll(e.value as Map<String, Any?>)
            targetPackageJson[e.key] = newval
        } else {
            targetPackageJson[e.key] = e.value
        }
    }
    jsonIo.objectToPath(targetPackageJson, targetPath)
}
