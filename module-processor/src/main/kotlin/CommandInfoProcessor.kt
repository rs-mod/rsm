import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration


class CommandInfoProcessor (
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        val classes = resolver.getSymbolsWithAnnotation("com.ricedotwho.rsm.command.api.CommandInfo")
            .filterIsInstance<KSClassDeclaration>()
            .toList()
        val names = classes.mapNotNull { it.qualifiedName?.asString() }
        if (names.isEmpty()) return emptyList()
        invoked = true
        val sourceFiles = classes.mapNotNull { it.containingFile }.toTypedArray()

        val file = codeGenerator.createNewFile(
            Dependencies(aggregating = true, *sourceFiles),
            "com.ricedotwho.rsm.core",
            "GeneratedRegistrationList",
            "kt"
        )
        val imports = names.joinToString("\n") { "import $it" }
        val commands = names.joinToString(",") { "${it.substringAfterLast('.')}::class.java" }
        val javaFile = """
package com.ricedotwho.rsm.core

import com.ricedotwho.rsm.command.Command
$imports

object GeneratedCommandList {
    val commands: List<Class<out Command>> = listOf(
        $commands
    )
}
        """.trimIndent()
        file.write(javaFile.toByteArray())

        return emptyList()
    }
}