import org.jetbrains.amper.plugins.Configurable


enum class IndentationFormat {
    TABS, SPACES
}

enum class Baseline {
    J8, J17
}


@Configurable
interface Settings {
    val indentation: IndentationFormat get() = IndentationFormat.SPACES
    val baseline: Baseline get() = Baseline.J8
    val encoding: String get() = "UTF-8"
}
