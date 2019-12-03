package org.codejive.kodesta.cli

import com.github.ajalt.clikt.core.CliktCommand
import org.codejive.kodesta.core.resource.generate

class Generate : CliktCommand(help = "Generates all the OpenShift templates needed for the Creator to work") {
    override fun run() {
        generate()
    }
}
