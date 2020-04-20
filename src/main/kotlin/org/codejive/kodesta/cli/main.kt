package org.codejive.kodesta.cli

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.subcommands
import org.codejive.kodesta.catalog.GeneratorRegistry

class Creator : NoRunCliktCommand() {
    val registry by findOrSetObject { GeneratorRegistry() }
    init {
        context { allowInterspersedArgs = false }
    }
}

fun main(args: Array<String>) = Creator()
    .subcommands(
        Apply(),
        Generate(),
        Analyze(),
        CreatorList().subcommands(
            ListGenerators(),
            ListRuntimes()
        )
    )
    .main(args)
