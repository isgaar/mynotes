// File: app/src/main/java/com/iotaslife/app/domain/model/TicketTaskHelper.kt
// Description: Utility helpers for linking tasks to bitacora tickets without schema mutations, preserving full database integrity.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.model

object TicketTaskHelper {
    private val TICKET_TAG_REGEX = Regex("""\[(TIK-[0-9A-Za-z_-]+)\]""")

    /**
     * Formats a task description referencing a bitacora ticket code.
     */
    fun formatDescriptionWithTicket(ticketCode: String, notes: String): String {
        val cleanCode = ticketCode.trim()
        val cleanNotes = notes.trim()
        return if (cleanNotes.isBlank()) {
            "[$cleanCode]"
        } else {
            "[$cleanCode] $cleanNotes"
        }
    }

    /**
     * Checks if this task belongs to the given ticket code.
     */
    fun TaskItem.isLinkedToTicket(ticketCode: String): Boolean {
        val cleanCode = ticketCode.trim()
        if (cleanCode.isBlank()) return false
        return description.contains("[$cleanCode]", ignoreCase = true) ||
                title.contains("[$cleanCode]", ignoreCase = true) ||
                description.contains("Ticket: $cleanCode", ignoreCase = true)
    }

    /**
     * Extracts ticket code from task description or title, if any.
     */
    fun TaskItem.getLinkedTicketCode(): String? {
        val matchDesc = TICKET_TAG_REGEX.find(description)
        if (matchDesc != null) return matchDesc.groupValues[1]

        val matchTitle = TICKET_TAG_REGEX.find(title)
        if (matchTitle != null) return matchTitle.groupValues[1]

        return null
    }

    /**
     * Returns clean description stripped of ticket code bracket tags.
     */
    fun TaskItem.getCleanDescription(): String {
        return description.replace(TICKET_TAG_REGEX, "").trim()
    }
}
