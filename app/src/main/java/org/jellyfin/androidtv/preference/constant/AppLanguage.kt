package org.jellyfin.androidtv.preference.constant

/**
 * Represents the available app languages.
 * The values correspond to the language codes used in the res/values-* directories.
 */
enum class AppLanguage(
    /**
     * The language code used in res/values-* directories
     */
    val code: String,
    /**
     * The display name of the language in its native form
     */
    val displayName: String
) {
    // System default (follows device settings)
    SYSTEM_DEFAULT("", "System Default"),
    
    // Supported languages
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    ITALIAN("it", "Italiano"),
    DUTCH("nl", "Nederlands"),
    PORTUGUESE("pt", "Português"),
    RUSSIAN("ru", "Русский"),
    JAPANESE("ja", "日本語"),
    KOREAN("ko", "한국어"),
    CHINESE_SIMPLIFIED("zh-rCN", "简体中文"),
    CHINESE_TRADITIONAL("zh-rTW", "繁體中文"),
    ARABIC("ar", "العربية"),
    HINDI("hi", "हिन्दी"),
    TURKISH("tr", "Türkçe"),
    POLISH("pl", "Polski"),
    UKRAINIAN("uk", "Українська"),
    VIETNAMESE("vi", "Tiếng Việt"),
    THAI("th", "ไทย"),
    GREEK("el", "Ελληνικά"),
    HEBREW("he", "עברית"),
    FINNISH("fi", "Suomi"),
    SWEDISH("sv", "Svenska"),
    NORWEGIAN("nb", "Norsk"),
    DANISH("da", "Dansk"),
    CZECH("cs", "Čeština"),
    HUNGARIAN("hu", "Magyar"),
    ROMANIAN("ro", "Română"),
    INDONESIAN("id", "Bahasa Indonesia"),
    MALAY("ms", "Bahasa Melayu"),
    BULGARIAN("bg", "Български"),
    CROATIAN("hr", "Hrvatski"),
    SLOVAK("sk", "Slovenčina"),
    SLOVENIAN("sl", "Slovenščina"),
    LITHUANIAN("lt", "Lietuvių"),
    LATVIAN("lv", "Latviešu"),
    ESTONIAN("et", "Eesti"),
    SERBIAN("sr", "Српски"),
    MACEDONIAN("mk", "Македонски"),
    MONGOLIAN("mn", "Монгол"),
    KAZAKH("kk", "Қазақ"),
    AZERBAIJANI("az", "Azərbaycan"),
    GEORGIAN("ka", "ქართული"),
    ARMENIAN("hy", "Հայերեն"),
    ALBANIAN("sq", "Shqip"),
    BOSNIAN("bs", "Bosanski"),
    CATALAN("ca", "Català"),
    WELSH("cy", "Cymraeg"),
    IRISH("ga", "Gaeilge"),
    SCOTTISH_GAELIC("gd", "Gàidhlig"),
    BASQUE("eu", "Euskara"),
    GALICIAN("gl", "Galego"),
    ICELANDIC("is", "Íslenska");

    companion object {
        /**
         * Get the AppLanguage enum from a language code
         * @param code The language code to look up
         * @return The matching AppLanguage or SYSTEM_DEFAULT if not found
         */
        fun fromCode(code: String?): AppLanguage {
            if (code.isNullOrEmpty()) return SYSTEM_DEFAULT
            return values().find { it.code == code } ?: SYSTEM_DEFAULT
        }
    }
}
