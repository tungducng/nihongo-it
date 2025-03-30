package com.example.japanesitlearning.entity


enum class LessonCategory(
    val displayName: String,
    val description: String,
    val icon: String
) {
    // IT-specific categories
    IT_FUNDAMENTALS("IT Fundamentals", "Basic IT terminology and concepts in Japanese", "computer"),
    PROGRAMMING("Programming", "Programming concepts and terminology in Japanese", "code"),
    WEB_DEVELOPMENT("Web Development", "Web development terminology in Japanese", "web"),
    DATABASE("Database Systems", "Database concepts and SQL terminology in Japanese", "database"),
    CYBERSECURITY("Cybersecurity", "Security concepts and terminology in Japanese", "security"),

    // General Japanese categories
    DAILY_CONVERSATION("Daily Conversation", "Common phrases for everyday situations", "chat"),
    BUSINESS_JAPANESE("Business Japanese", "Professional communication in Japanese workplace", "business"),
    READING_PRACTICE("Reading Practice", "Texts for improving reading comprehension", "book"),
    KANJI_STUDY("Kanji Study", "Learning and practicing Kanji characters", "kanji"),
    GRAMMAR_BASICS("Grammar Basics", "Fundamental Japanese grammar structures", "grammar"),

    // Level-based categories
    JLPT_N5("JLPT N5 Preparation", "Beginner level vocabulary and grammar", "n5"),
    JLPT_N4("JLPT N4 Preparation", "Basic level vocabulary and grammar", "n4"),
    JLPT_N3("JLPT N3 Preparation", "Intermediate level vocabulary and grammar", "n3"),

    // Specialized content
    JAPANESE_CULTURE("Japanese Culture", "Cultural context for better language understanding", "culture"),
    TECHNICAL_DOCUMENTS("Technical Documents", "Reading and writing technical documents in Japanese", "document"),
    JOB_INTERVIEW("Job Interview", "Preparing for job interviews in Japanese", "interview");

    companion object {
        fun forJlptLevel(level: Int): List<LessonCategory> {
            return when(level) {
                5 -> listOf(JLPT_N5, DAILY_CONVERSATION, IT_FUNDAMENTALS)
                4 -> listOf(JLPT_N4, BUSINESS_JAPANESE, PROGRAMMING)
                3 -> listOf(JLPT_N3, TECHNICAL_DOCUMENTS, DATABASE, CYBERSECURITY)
                else -> values().toList()
            }
        }

        fun forItFocus(): List<LessonCategory> {
            return listOf(IT_FUNDAMENTALS, PROGRAMMING, WEB_DEVELOPMENT, DATABASE, CYBERSECURITY, TECHNICAL_DOCUMENTS)
        }
    }
}