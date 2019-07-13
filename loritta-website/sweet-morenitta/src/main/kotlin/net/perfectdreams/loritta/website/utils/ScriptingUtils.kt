package net.perfectdreams.loritta.website.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.website.LorittaWebsite
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import java.io.File
import java.util.*

object ScriptingUtils {
    private val logger = KotlinLogging.logger {}

    fun <T> evaluateTemplate(file: File, args: Map<String, String> = mapOf()): T {
        if (LorittaWebsite.INSTANCE.pathCache[file] != null)
            return LorittaWebsite.INSTANCE.pathCache[file] as T

        val code = generateCodeToBeEval(file)
            .replace("@args", args.entries.joinToString(", ", transform = { "${it.key}: ${it.value}"}))
            .replace("@call-args", args.keys.joinToString(", "))

        File("${LorittaWebsite.INSTANCE.config.websiteFolder}/generated_views/${file.name}").writeText(code)

        logger.info("Compiling ${file.name}...")

        val millis = measureTimeMillisWithResult {
            val test = KtsObjectLoader().load<Any>(
                """
                import kotlinx.html.*
                import kotlinx.html.dom.*
                import net.perfectdreams.loritta.website.utils.KtsObjectLoader
                import net.perfectdreams.loritta.utils.*
                import net.perfectdreams.loritta.utils.locale.*
                import net.perfectdreams.loritta.website.*
                import net.perfectdreams.loritta.api.entities.*
                import net.perfectdreams.loritta.website.utils.config.*
                import org.w3c.dom.Document
                import org.w3c.dom.Element
                import java.io.File
                import net.perfectdreams.temmiediscordauth.*

                $code
            """.trimIndent()
            )

            LorittaWebsite.INSTANCE.pathCache[file] = test

            return@measureTimeMillisWithResult test
        }

        logger.info("Took ${millis.first}ms to compile ${file.name}!")

        return millis.second as T
    }

    fun generateCodeToBeEval(file: File): String {
        val stack = fillStack(file, Stack())

        val output = StringBuilder()

        while (!stack.empty()) {
            val holder = stack.pop()
            val tempCode = generateCodeFromFile(holder.file, holder.code)
            output.append(tempCode)
            output.append('\n')

            if (stack.empty()) {
                output.append(holder.file.nameWithoutExtension.capitalize())
                output.append("()")
            }
        }

        return output.toString()
    }

    fun generateCodeFromFile(f: File, code: List<String>): String {
        var isAbstract = false
        var classToBeExtended: String? = null

        val importedCode = mutableListOf<String>()

        // Preprocess stage
        for (line in code) {
            if (line == "@type 'abstract'")
                isAbstract = true
            if (line.startsWith("@extends")) {
                val pathToBeImported = line.substring("@extends '".length, line.length - 1)

                val fileName = pathToBeImported.replace(".kts", "")
                classToBeExtended = fileName.capitalize()
            }
            if (line.startsWith("@import")) {
                val pathToBeImported = line.substring("@import '".length, line.length - 1)

                importedCode.add(File("${LorittaWebsite.INSTANCE.config.websiteFolder}/views/$pathToBeImported").readText())
                continue
            }
        }

        // Generate the code
        var tempCode = ""
        if (isAbstract)
            tempCode += "abstract "
        else
            tempCode += "open "
        tempCode += "class ${f.nameWithoutExtension.capitalize()} "
        if (classToBeExtended != null)
            tempCode += ": $classToBeExtended() "
        tempCode += "{\n"

        importedCode.forEach {
            tempCode += it
        }

        for (line in code) {
            if (!line.startsWith("@")) {
                tempCode += "    $line\n"
            }
        }

        tempCode += "}\n"

        return tempCode
    }

    fun fillStack(f: File, stack: Stack<CodeHolder>): Stack<CodeHolder> {
        val inputLines = f.readLines()
        val firstLine = inputLines.first()

        stack.push(
            CodeHolder(
                f,
                inputLines
            )
        )

        if (firstLine.startsWith("@extends")) {
            val pathToBeExtended = firstLine.substring("@extends '".length, firstLine.length - 1)

            fillStack(File("${LorittaWebsite.INSTANCE.config.websiteFolder}/views/$pathToBeExtended"), stack)
        }
        return stack
    }

    data class CodeHolder(
        val file: File,
        val code: List<String>
    )
}