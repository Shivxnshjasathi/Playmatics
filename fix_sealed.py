import re

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/VariantMetadata.kt", "r") as f:
    content = f.read()

# Fix the placement of the closing brace
content = content.replace("    }\n}\n    // ── Crossword", "    }\n\n    // ── Crossword")
content = content.replace("    ) : VariantMetadata()\n}\n", "    ) : VariantMetadata()\n}")

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/VariantMetadata.kt", "w") as f:
    f.write(content)
