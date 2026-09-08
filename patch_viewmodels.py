import re

def patch_vm(file_path):
    with open(file_path, "r") as f:
        content = f.read()
    
    if "fun onAction" not in content:
        action_func = """
    fun onAction(action: String) {
        when (action) {
            "PLAY_WIN" -> audioPlayer.playWin()
            "PLAY_ERROR" -> audioPlayer.playError()
            "PLAY_CLICK" -> audioPlayer.playClick()
        }
    }
}"""
        content = re.sub(r"\}\s*$", action_func, content)
        with open(file_path, "w") as f:
            f.write(content)

patch_vm("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerViewModel.kt")
patch_vm("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt")
