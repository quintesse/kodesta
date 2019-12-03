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

    fun capabilities() = generators.filter { it.name.startsWith("capability-") }

    fun capability(name: String): GeneratorInfo {
        if (!name.startsWith("capability-")) {
            try {
                return byName("capability-$name")
            } catch (ex: IllegalArgumentException) { /* ignore */
            }
        }
        return byName(name)
    }

    val capabilityInfoDefs by lazy { capabilities().map { it.infoDef } }

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

    companion object {
        @JvmStatic
        val defaultRegistry : GeneratorRegistry by lazy {
            // TODO Make this non-hardcoded
            GeneratorRegistry()
                .add("capability-component", ::CapabilityComponent)
                .add("capability-database", ::CapabilityDatabase)
                .add("capability-health", ::CapabilityHealth)
                .add("capability-import", ::CapabilityImport)
                .add("capability-rest", ::CapabilityRest)
                .add("capability-web-app", ::CapabilityWebApp)
                .add("capability-welcome", ::CapabilityWelcome)
                .add("app-images", ::AppImages)
                .add("database-crud-dotnet")
                .add("database-crud-nodejs")
                .add("database-crud-quarkus")
                .add("database-crud-springboot")
                .add("database-crud-thorntail")
                .add("database-crud-vertx")
                .add("database-crud-wildfly", ::DatabaseCrudWildfly)
                .add("database-mysql")
                .add("database-postgresql")
                .add("database-secret")
                .add("import-codebase", ::ImportCodebase)
                .add("language-csharp")
                .add("language-java")
                .add("language-nodejs")
                .add("maven-setup", ::MavenSetup)
                .add("runtime-angular")
                .add("runtime-base-support", ::RuntimeBaseSupport)
                .add("runtime-dotnet")
                .add("runtime-nodejs")
                .add("runtime-quarkus")
                .add("runtime-react")
                .add("runtime-springboot")
                .add("runtime-thorntail")
                .add("runtime-vertx")
                .add("runtime-vuejs")
                .add("runtime-wildfly")
                .add("rest-dotnet")
                .add("rest-nodejs")
                .add("rest-quarkus")
                .add("rest-springboot")
                .add("rest-thorntail")
                .add("rest-vertx")
                .add("rest-wildfly")
                .add("welcome-app", ::WelcomeApp)
        }
    }
}

data class GeneratorInfo internal constructor (val name: String, val klazz: GeneratorConstructor = ::SimpleConfigGenerator) {
    val infoDef by lazy { readGeneratorInfoDef(this.name) }
}
