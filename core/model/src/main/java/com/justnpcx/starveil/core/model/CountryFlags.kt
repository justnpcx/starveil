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

    /**
     * The mark for a node whose country is unknown.
     *
     * A globe rather than nothing: every row then begins with a region mark of
     * the same size, and "we could not place this one" is said instead of
     * leaving the name to start in a different column.
     */
    const val UNKNOWN = "🌐"
}

/**
 * The name as the UI shows it: `🇺🇸 HY2+Reality`, or `🌐 npc-home` when the
 * country could not be resolved. The stored name never changes — the mark is a
 * rendering detail, so exports and backups stay exactly what the provider
 * wrote.
 *
 * The globe is a deliberate fallback, not an error state: a node whose address
 * is private or otherwise not in the GeoIP database still works, and a region
 * mark on every row keeps the names starting at the same place. A globe is one
 * code point, so the dashboard's leading-mark split leaves it in the name
 * string and it renders inline at the name's own size.
 */
fun Profile.displayName(): String = "${CountryFlags.flag(country) ?: CountryFlags.UNKNOWN} $name"
