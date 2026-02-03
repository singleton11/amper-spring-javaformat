import org.jetbrains.amper.plugins.Configurable


enum class IndentationFormat {
    TABS, SPACES
}

enum class Baseline {
    J8, J17
}


@Configurable
interface Settings {
    val indentation: IndentationFormat?
    val baseline: Baseline?
    val encoding: String?
}
