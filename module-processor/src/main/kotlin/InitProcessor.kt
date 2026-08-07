import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration


class InitProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        val functions = resolver.getSymbolsWithAnnotation("com.ricedotwho.rsm.core.Init")
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        if (functions.isEmpty()) return emptyList()

        val sourceFiles = functions.mapNotNull { it.containingFile }.toTypedArray()
        val file = codeGenerator.createNewFile(
            Dependencies(aggregating = true, *sourceFiles),
            "com.ricedotwho.rsm.core",
            "GeneratedInitList",
            "java"
        )
        val calls = functions.joinToString(",\n        ") {
            val cls = it.parentDeclaration as? KSClassDeclaration
            "${cls?.qualifiedName?.asString()}.class"
        }
        val kotlinFile = """
            package com.ricedotwho.rsm.core;

            import java.util.List;
            
            public class GeneratedInitList {
                public static final List<Class<?>> initClasses = List.of(
$calls
                );
            }
        """.trimIndent()

        file.write(kotlinFile.toByteArray())
        invoked = true

        return emptyList()
    }
}
