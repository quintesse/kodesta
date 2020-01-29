package org.codejive.kodesta.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.context
import org.codejive.kodesta.catalog.GeneratorRegistry
import org.codejive.kodesta.core.enumById

class CreatorList : NoRunCliktCommand(name="list", help="Lists possible values for generators and runtimes") {
    init {
        context { allowInterspersedArgs = false }
    }
}

class ListGenerators : CliktCommand(name="generators", help = "Lists possible values for generators") {
    override fun run() {
        GeneratorRegistry
            .defaultRegistry
            .generators
            .map { it.name.substring(11) }
            .sorted()
            .forEach { println(it) }
    }
}

class ListRuntimes : CliktCommand(name = "runtimes", help = "Lists possible values for runtimes") {
    override fun run() {
        GeneratorRegistry
            .defaultRegistry
            .enums
            .enumById("runtime.name").map { it.id }.sorted().forEach { println(it) }
    }
}
