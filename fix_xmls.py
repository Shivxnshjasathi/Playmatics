import os

files = [
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml"
]

for file_path in files:
    with open(file_path, "r") as f:
        content = f.read()
    
    xml_decl = '<?xml version="1.0" encoding="utf-8"?>\n'
    if xml_decl in content and not content.startswith('<?xml'):
        # Remove the declaration from wherever it is
        content = content.replace(xml_decl, '')
        # Put it at the very top
        content = xml_decl + content
        
        with open(file_path, "w") as f:
            f.write(content)
        print(f"Fixed {file_path}")

