#include "Include.h"

void TestMenu() {
    if (InitHook) {
        if (ImGui::Button("Open URL")) {
            OpenURL("https://t.me/evgeniy1998zz");
        }
    } else {
        ImGui::Text("Hooks not loaded");
    }
}
void (*old_Player_Update)(void*) = nullptr;
void (*old_Player_OnDestroy)(void*) = nullptr;

void Player_Update(void* player) {
    if (player != nullptr) {
        if (!playerFind(player)) {
            players.push_back(player);
        }
    }
    if (old_Player_Update)
        old_Player_Update(player);
}

void Player_OnDestroy(void* player) {
    if (player != nullptr) {
        if (old_Player_OnDestroy)
            old_Player_OnDestroy(player);

        auto it = std::find(players.begin(), players.end(), player);
        if (it != players.end())
            players.erase(it);
    }
}
void ESPDraw() {
    if (!isESP || !InitHook) return;

    void* camera = get_main();
    if (!camera) return;

    for (auto& player : players) {
        if (!player) continue;

        Vector3 pos3D = getPosition(player);
        Vector3 pos2D = WorldToScreen(camera, pos3D);

        float x = pos2D.x;
        float y = glHeight - pos2D.y;

        if (x <= 0 || y <= 0 || x >= glWidth || y >= glHeight)
            continue;

        float width = 50.0f;
        float height = 100.0f;
        Rect boxRect(x - width / 2, y - height, width, height);

        if (isESP) {
            Draw::Box(boxRect, ImVec4(1.0f, 0.0f, 0.0f, 1.0f));
        }

        if (isESPLine) {
            Draw::Line(ImVec2(glWidth / 2, glHeight), ImVec2(x, y), ImVec4(0.0f, 1.0f, 0.0f, 1.0f), 1.5f);
        }
    }
}