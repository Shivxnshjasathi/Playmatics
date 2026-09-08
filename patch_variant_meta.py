import re

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/VariantMetadata.kt", "r") as f:
    content = f.read()

# Remove ScrabbleBoard
old_block = r"""    // ── Scrabble ─────────────────────────────────────────────────────
    /\*\*
     \* Map of premium squares for Scrabble\.
     \* 0 = Normal, 1 = DL, 2 = TL, 3 = DW, 4 = TW
     \*/
    data class ScrabbleBoard\(val premiumMap: Array<IntArray>\) : VariantMetadata\(\) \{
        override fun equals\(other: Any\?\): Boolean \{
            if \(this === other\) return true
            if \(other !is ScrabbleBoard\) return false
            return premiumMap\.contentDeepEquals\(other\.premiumMap\)
        \}
        override fun hashCode\(\): Int = premiumMap\.contentDeepHashCode\(\)
    \}

"""

content = re.sub(old_block, "", content)

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/VariantMetadata.kt", "w") as f:
    f.write(content)
