import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/WordSearchBoard.kt", "r") as f:
    content = f.read()

old_sig = """fun WordSearchBoard(
    board: Array<IntArray>,
    variantMetadata: VariantMetadata?,
    modifier: Modifier = Modifier
) {"""
new_sig = """fun WordSearchBoard(
    board: Array<IntArray>,
    variantMetadata: VariantMetadata?,
    onWordFound: () -> Unit = {},
    modifier: Modifier = Modifier
) {"""
content = content.replace(old_sig, new_sig)

old_found_add = """                                if (meta.words.contains(selectedWord) && !foundWords.contains(selectedWord)) {
                                    foundWords.add(selectedWord)
                                    foundLines.add(start to end)
                                } else if (meta.words.contains(reversedWord) && !foundWords.contains(reversedWord)) {
                                    foundWords.add(reversedWord)
                                    foundLines.add(start to end)
                                }"""
new_found_add = """                                if (meta.words.contains(selectedWord) && !foundWords.contains(selectedWord)) {
                                    foundWords.add(selectedWord)
                                    foundLines.add(start to end)
                                    onWordFound()
                                } else if (meta.words.contains(reversedWord) && !foundWords.contains(reversedWord)) {
                                    foundWords.add(reversedWord)
                                    foundLines.add(start to end)
                                    onWordFound()
                                }"""
content = content.replace(old_found_add, new_found_add)

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/WordSearchBoard.kt", "w") as f:
    f.write(content)
