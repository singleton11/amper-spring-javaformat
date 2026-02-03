import io.spring.javaformat.config.IndentationStyle
import io.spring.javaformat.config.JavaBaseline
import io.spring.javaformat.config.JavaFormatConfig
import io.spring.javaformat.formatter.FileEdit
import io.spring.javaformat.formatter.FileFormatter
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.ModuleSources
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension

private fun Baseline?.toJavaBaseline(): JavaBaseline = when (this ?: Baseline.J8) {
    Baseline.J8 -> JavaBaseline.V8
    Baseline.J17 -> JavaBaseline.V17
}

private fun IndentationFormat?.toIndentationStyle(): IndentationStyle = when (this ?: IndentationFormat.SPACES) {
    IndentationFormat.TABS -> IndentationStyle.TABS
    IndentationFormat.SPACES -> IndentationStyle.SPACES
}

private fun ModuleSources.findJavaFiles(): List<File> =
    sourceDirectories
        .flatMap { Files.walk(it).toList() }
        .filter { it.extension == "java" }
        .map { it.toFile() }

private fun formatJavaFiles(
    sources: ModuleSources,
    javaBaseline: Baseline?,
    indentationFormat: IndentationFormat?,
    encoding: String?
): List<FileEdit> {
    val config = JavaFormatConfig.of(
        javaBaseline.toJavaBaseline(),
        indentationFormat.toIndentationStyle()
    )
    val charset = encoding?.let { Charset.forName(it) } ?: Charset.defaultCharset()
    return FileFormatter(config)
        .formatFiles(sources.findJavaFiles(), charset)
        .filter { it.hasEdits() }
        .toList()
}

private fun ensureReportFileExists(report: Path) {
    if (!report.exists()) {
        report.createParentDirectories()
        report.createFile()
    }
}

@TaskAction
fun generateReport(
    @Input sources: ModuleSources,
    javaBaseline: Baseline?,
    indentationFormat: IndentationFormat?,
    encoding: String?,
    @Output report: Path
) {
    val edits = formatJavaFiles(sources, javaBaseline, indentationFormat, encoding)
    if (edits.isEmpty()) {
        println("Java code is formatted correctly.")
        return
    }
    ensureReportFileExists(report)
    report.toFile().writeText(edits.joinToString("\n") { "${it.file.absolutePath}:\n${it.formattedContent}" })
    error("Java code is not formatted correctly. See report at ${report.toUri()}")
}

@TaskAction
fun format(
    @Input sources: ModuleSources,
    javaBaseline: Baseline?,
    indentationFormat: IndentationFormat?,
    encoding: String?,
    @Output report: Path
) {
    val edits = formatJavaFiles(sources, javaBaseline, indentationFormat, encoding)
    ensureReportFileExists(report)
    edits.forEach { it.save() }
    println("Java code formatted successfully.")
}
