package org.codejive.kodesta.catalog

import org.codejive.kodesta.core.*
import org.codejive.kodesta.core.catalog.GeneratorConstructor
import org.codejive.kodesta.core.catalog.SimpleConfigGenerator
import org.codejive.kodesta.core.catalog.readGeneratorInfoDef
import org.codejive.kodesta.core.data.objectFromPath
import org.codejive.kodesta.core.data.yamlIo
import java.nio.file.Paths

class GeneratorRegistry() {
    private val _generators: MutableMap<String, GeneratorInfo> = mutableMapOf()

    val generators get() = _generators.values

    fun byName(name: String) = generators.find { it.name == name }
        ?: throw IllegalArgumentException("Undefined generator: '$name', should be one of: $generators")

    fun add(name: String, klazz: GeneratorConstructor = ::SimpleConfigGenerator) : GeneratorRegistry {
        if (!_generators.containsKey(name)) {
            _generators[name] = GeneratorInfo(name, klazz)
        }
        return this
    }

    val infoDefs by lazy { generators.map { it.infoDef } }

    val enums: Enums by lazy {
        val enumsFile= Paths.get("META-INF/catalog/enums.yaml")
        if (existsFromPath(enumsFile)) {
            val props = yamlIo.objectFromPath(enumsFile) as Properties
            props.keys.forEach { key ->
                props[key] = ensureList(key, props[key], Enumeration::Data)
            }
            props as Enums
        } else {
            propsOf() as Enums
        }
    }
}

data class GeneratorInfo internal constructor (val name: String, val klazz: GeneratorConstructor = ::SimpleConfigGenerator) {
    val infoDef by lazy { readGeneratorInfoDef(this.name) }
}
