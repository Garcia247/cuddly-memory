import webview

APP_URL = "https://upwork-proposal-intelligence-agent.vercel.app/"

if __name__ == "__main__":
    webview.create_window(
        "Upwork Proposal Intelligence Agent",
        APP_URL,
        width=1280,
        height=860,
        min_size=(900, 640),
        confirm_close=False,
    )
    webview.start(gui="edgechromium", private_mode=False)
