import glob
import os

for file in glob.glob("app/src/main/java/com/zincstate/playmatics/data/local/dao/*.kt"):
    with open(file, "r") as f:
        content = f.read()
    
    if "@JvmSuppressWildcards" not in content:
        content = content.replace("@Dao", "@Dao\n@JvmSuppressWildcards")
        
        with open(file, "w") as f:
            f.write(content)

