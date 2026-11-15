package com.justnpcx.starveil.core.model

/**
 * Flags from ISO codes.
 *
 * A regional-indicator flag is just the two letters shifted into the emoji
 * block, so there is nothing to maintain: every code that renders has a flag,
 * and no table can drift from the codes it claims to cover. A device without
 * flag glyphs shows the two letters, which is still readable.
 */
object CountryFlags {

    fun flag(iso: String?): String? {
        val code = iso?.trim()?.uppercase() ?: return null
        if (code.length != 2 || !code.all { it in 'A'..'Z' }) return null
        // A code point, not a Char: the flags live above the BMP, so `Char`
        // arithmetic would wrap into the private-use area and render as two
        // boxes.
        return buildString {
            code.forEach { letter -> appendCodePoint(REGIONAL_INDICATOR_A + (letter - 'A')) }
        }
    }

    private const val REGIONAL_INDICATOR_A = 0x1F1E6

}

/**
 * The name as the UI shows it: `🇺🇸 HY2+Reality`, or the bare name when the
 * country could not be resolved. The stored name never changes — the mark is a
 * rendering detail, so exports and backups stay exactly what the provider
 * wrote.
 *
 * No country means no mark. There is no placeholder flag: a row that has not
 * been placed starts with the name, and every row that does have a country
 * starts with it. The resolver's job is to place a node — the device's region
 * stands in when the database cannot — so a bare name is the rare case where
 * even that was unavailable, and a flag there would be a guess dressed up as
 * a fact.
 */
fun Profile.displayName(): String =
    CountryFlags.flag(country)?.let { "$it $name" } ?: name
