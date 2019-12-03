package org.codejive.kodesta.core.catalog

import org.codejive.kodesta.core.data.objectFromPath
import org.codejive.kodesta.core.data.yamlIo
import org.codejive.kodesta.core.deepClone
import java.nio.file.Path
import java.nio.file.Paths

fun readGeneratorInfoDef(generatorName: String): ModuleInfoDef {
    try {
        return readInfoDef(generatorName, Paths.get("META-INF/catalog/${generatorName}/info.yaml"))
    } catch (ex: Exception) {
        throw RuntimeException("No info found for generator '${generatorName}'", ex)
    }
}

private fun readInfoDef(name: String, infoFile: Path): ModuleInfoDef {
    val obj = yamlIo.objectFromPath(infoFile).deepClone()
    obj["module"] = name
    return ModuleInfoDef.build(obj)
}
