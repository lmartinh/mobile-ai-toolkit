package dev.mobileai.toolkit.composeguardrails.core.prompt

enum class RuleSet(val cliValue: String) {
    DEFAULT("default"),
    ADVANCED("advanced"),
    ALL("all");

    companion object {
        fun fromCli(value: String): RuleSet {
            return entries.firstOrNull { it.cliValue == value.lowercase() }
                ?: throw IllegalArgumentException(
                    "Invalid rule set: $value. Supported values: default, advanced, all"
                )
        }
    }
}
