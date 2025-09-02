package org.jellyfin.androidtv.preference.constant

/**
 * Represents the available audio languages.
 * The values correspond to the language codes used for audio tracks.
 */
enum class AudioLanguage(
    /**
     * The language code used for audio tracks
     */
    val code: String,
    /**
     * The display name of the language
     */
    val displayName: String
) {
    // None/Default option (no preference)
    NONE("", "(Default)"),
    
    // Supported audio languages with format: English (Native)
    // Using ISO 639-1/2 language codes compatible with Android AudioTrack
    ARABIC("ar", "Arabic (العربية)"),
    CHINESE("zh", "Chinese (中文)"),
    CZECH("cs", "Czech (Čeština)"),
    DANISH("da", "Danish (Dansk)"),
    DUTCH("nl", "Dutch (Nederlands)"),
    ENGLISH("en", "English (English)"),
    FINNISH("fi", "Finnish (Suomi)"),
    FRENCH("fr", "French (Français)"),
    GERMAN("de", "German (Deutsch)"),
    GREEK("el", "Greek (Ελληνικά)"),
    HEBREW("he", "Hebrew (עברית)"),
    HINDI("hi", "Hindi (हिन्दी)"),
    HUNGARIAN("hu", "Hungarian (Magyar)"),
    INDONESIAN("id", "Indonesian (Bahasa Indonesia)"),
    ITALIAN("it", "Italian (Italiano)"),
    JAPANESE("ja", "Japanese (日本語)"),
    KOREAN("ko", "Korean (한국어)"),
    MALAY("ms", "Malay (Bahasa Melayu)"),
    NORWEGIAN("no", "Norwegian (Norsk)"),
    POLISH("pl", "Polish (Polski)"),
    PORTUGUESE("pt", "Portuguese (Português)"),
    ROMANIAN("ro", "Romanian (Română)"),
    RUSSIAN("ru", "Russian (Русский)"),
    SLOVAK("sk", "Slovak (Slovenčina)"),
    SPANISH("es", "Spanish (Español)"),
    SWEDISH("sv", "Swedish (Svenska)"),
    THAI("th", "Thai (ไทย)"),
    TURKISH("tr", "Turkish (Türkçe)"),
    UKRAINIAN("uk", "Ukrainian (Українська)"),
    VIETNAMESE("vi", "Vietnamese (Tiếng Việt)");

    companion object {
        /**
         * Finds an [AudioLanguage] by its code.
         * Handles both ISO 639-1 (2-letter) and ISO 639-2 (3-letter) codes.
         *
         * @param code The language code to search for.
         * @return The matching [AudioLanguage] or `null` if not found.
         */
        fun fromCode(code: String?): AudioLanguage? {
            if (code.isNullOrEmpty()) return null
            
            // First try exact match
            values().find { it.code.equals(code, ignoreCase = true) }?.let { return it }
            
            // Handle common 3-letter codes that might be in media files
            return when (code.lowercase()) {
                "ara" -> ARABIC
                "zho", "chi" -> CHINESE
                "ces", "cze" -> CZECH
                "dan" -> DANISH
                "nld", "dut" -> DUTCH
                "eng" -> ENGLISH
                "fin" -> FINNISH
                "fra", "fre" -> FRENCH
                "deu", "ger" -> GERMAN
                "ell", "gre" -> GREEK
                "heb" -> HEBREW
                "hin" -> HINDI
                "hun" -> HUNGARIAN
                "ind" -> INDONESIAN
                "ita" -> ITALIAN
                "jpn" -> JAPANESE
                "kor" -> KOREAN
                "msa", "may" -> MALAY
                "nor" -> NORWEGIAN
                "pol" -> POLISH
                "por" -> PORTUGUESE
                "ron", "rum" -> ROMANIAN
                "rus" -> RUSSIAN
                "slk", "slo" -> SLOVAK
                "spa" -> SPANISH
                "swe" -> SWEDISH
                "tha" -> THAI
                "tur" -> TURKISH
                "ukr" -> UKRAINIAN
                "vie" -> VIETNAMESE
                else -> null
            }
        }
    }

    override fun toString(): String = displayName
}
