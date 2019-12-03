package it

import org.codejive.kodesta.catalog.GeneratorRegistry
import org.codejive.kodesta.core.Runtime
import org.codejive.kodesta.core.catalog.ModuleInfoDef
import org.codejive.kodesta.core.enumNN
import org.codejive.kodesta.core.pathGet
import org.codejive.kodesta.core.runCmd
import java.nio.file.Path

fun getRuntimes(tier: String): List<String> {
    val rtOverrides = getRuntimeOverrides()
    return GeneratorRegistry.defaultRegistry.enums.enumNN("runtime.name")
        .filter { e -> e.pathGet<List<String>>("metadata.categories", listOf()).contains(tier) }
        .map { e -> e.id }
        .filter { rtid -> rtOverrides == null || rtOverrides.contains(rtid) }
}

fun getRuntimeVersions(tier: String): List<Runtime> {
    val rts = getRuntimes(tier)
    return rts.flatMap { rt ->
        GeneratorRegistry.defaultRegistry.enums.enumNN("runtime.version.$rt").map { v ->
            Runtime.build {
                name = rt
                version = v.id
            }
        }
    }
}

fun getCapabilities(tier: String): List<ModuleInfoDef> {
    val cOverrides = getCapabilityOverrides()
    val cis = GeneratorRegistry.defaultRegistry.capabilities()
    return cis
        .map { ci -> ci.infoDef }
        .filter { inf -> inf.pathGet("metadata.category", "") == tier }
        .filter { inf -> cOverrides == null || cOverrides.contains(inf.module) }
}

fun getServiceName(part: Part): String {
    return if (part.folder != null) "ittest-${part.folder}" else "ittest"
}

fun runTestCmd(cmd: String, vararg args: String?): String {
    return runTestCmd(null, cmd, *args)
}

fun runTestCmd(cwd: Path?, cmd: String, vararg args: String?): String {
    if (isVerbose()) {
        val cmdTxt = "$cmd ${args.joinToString(" ")}"
        System.out.println("      Run '$cmdTxt'")
    }
    if (!isDryRun()) {
        try {
            return runCmd(cwd, cmd, *args)
        } catch (ex: Exception) {
            if (isVerbose()) {
                System.err.println("ERROR: ${ex.message}")
            }
            throw ex
        }
    } else {
        return ""
    }
}
