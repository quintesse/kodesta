package org.codejive.kodesta.core.resource

import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.data.objectToString
import org.codejive.kodesta.core.data.yamlIo
import org.codejive.kodesta.core.existsFromPath
import org.codejive.kodesta.core.streamFromPath
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

// Returns the resources that were read from the given file
fun readResources(resourcesFile: Path): Resources {
    if (existsFromPath(resourcesFile)) {
        try {
            streamFromPath(resourcesFile).use {
                val map = yamlIo.objectFromStream(it)
                return Resources(map as Properties)
            }
        } catch (ex: Exception) {
            System.err.println("Failed to read resources file ${resourcesFile}: ${ex}")
            throw ex
        }
    } else {
        return Resources()
    }
}

// Writes the given resources to the given file
fun writeResources(resourcesFile: Path, res: Resources) {
    if (!res.isEmpty) {
        try {
            val str = yamlIo.objectToString(res.json)
            Files.createDirectories(resourcesFile.parent)
            resourcesFile.toFile().writeText(str)
        } catch (ex: Exception) {
            System.err.println("Failed to write resources file ${resourcesFile}: ${ex}")
            throw ex
        }
    }
}
