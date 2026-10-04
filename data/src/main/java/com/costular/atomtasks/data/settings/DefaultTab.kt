package com.costular.atomtasks.data.settings

enum class DefaultTab(val preferenceValue: String) {
    Agenda("agenda"),
    VerticalTasks("vertical_tasks"),
    ;

    companion object {
        fun fromString(value: String): DefaultTab =
            entries.firstOrNull { it.preferenceValue == value } ?: Agenda
    }
}
