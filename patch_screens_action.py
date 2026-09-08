import re

def patch_screen(file_path):
    with open(file_path, "r") as f:
        content = f.read()
    
    old_call = """                        onErase = viewModel::eraseCell,
                        onToggleNotes = viewModel::toggleNotesMode"""
    new_call = """                        onErase = viewModel::eraseCell,
                        onToggleNotes = viewModel::toggleNotesMode,
                        onAction = viewModel::onAction"""
    content = content.replace(old_call, new_call)
    
    with open(file_path, "w") as f:
        f.write(content)

patch_screen("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt")
patch_screen("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchScreen.kt")
