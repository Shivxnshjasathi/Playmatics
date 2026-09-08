import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt", "r") as f:
    content = f.read()

content = re.sub(r'import com\.zincstate\.playmatics\.presentation\.components\.DrawingBoard\n', '', content)
content = re.sub(r'import com\.zincstate\.playmatics\.presentation\.components\.PathStroke\n', '', content)
content = re.sub(r'import com\.zincstate\.playmatics\.domain\.engine\.VariantMetadata\.DrawingData\n', '', content)

drawing_block = r'                GameType\.DRAWING -> \{\n.*?\}\n'
content = re.sub(drawing_block, '', content, flags=re.DOTALL)

with open("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt", "w") as f:
    f.write(content)

