#include "Hook.h"
#include "ParticlesAnim.h"

#include "imgui.h"

static float menuScale = 1.0f;
static bool isCollapsed = false;
static bool firstOpen = true;

static ImVec2 windowPos;
static ImVec2 windowSize(900, 540);
static ImVec4 accentColor = ImVec4(0.4f, 0.7f, 1.0f, 1.0f);

bool enableCrosshair = false;
bool enableAimbot = false;
float aimbotFov = 65.0f;
bool aimbotDrawFov = false;

void ApplyCustomStyle() {
    ImGuiStyle& s = ImGui::GetStyle();
    s.WindowPadding = ImVec2(18, 18);
    s.FramePadding = ImVec2(14, 10);
    s.ItemSpacing = ImVec2(14, 12);
    s.ItemInnerSpacing = ImVec2(10, 8);
    s.WindowRounding = 16.0f;
    s.FrameRounding = 10.0f;
    s.GrabRounding = 8.0f;
    s.ScrollbarRounding = 8.0f;
    s.WindowBorderSize = 0.0f;
    s.WindowTitleAlign = ImVec2(0.5f, 0.5f);

    ImVec4 dark = ImVec4(0.08f, 0.10f, 0.14f, 0.95f);
    ImVec4 neon = accentColor;
    ImVec4 white = ImVec4(1, 1, 1, 1);
    ImVec4* c = s.Colors;
    c[ImGuiCol_WindowBg]             = dark;
    c[ImGuiCol_ChildBg]              = dark;
    c[ImGuiCol_Text]                 = white;
    c[ImGuiCol_Button]              = neon;
    c[ImGuiCol_ButtonHovered]       = ImVec4(neon.x + 0.1f, neon.y + 0.1f, neon.z + 0.1f, 1.0f);
    c[ImGuiCol_ButtonActive]        = neon;
    c[ImGuiCol_FrameBg]             = ImVec4(0.13f, 0.15f, 0.20f, 1.0f);
    c[ImGuiCol_FrameBgHovered]      = neon;
    c[ImGuiCol_FrameBgActive]       = neon;
    c[ImGuiCol_CheckMark]           = white;
    c[ImGuiCol_SliderGrab]          = white;
    c[ImGuiCol_SliderGrabActive]    = neon;
    c[ImGuiCol_Header]              = neon;
    c[ImGuiCol_HeaderHovered]       = white;
    c[ImGuiCol_HeaderActive]        = white;
    c[ImGuiCol_Tab]                 = ImVec4(0.20f, 0.22f, 0.30f, 1.0f);
    c[ImGuiCol_TabHovered]          = neon;
    c[ImGuiCol_TabActive]           = neon;
    c[ImGuiCol_TitleBg]             = dark;
    c[ImGuiCol_TitleBgActive]       = dark;
    c[ImGuiCol_TitleBgCollapsed]    = dark;
}

void DrawNeonText(const char* text, float width) {
    ImDrawList* draw = ImGui::GetWindowDrawList();
    ImVec2 pos = ImGui::GetCursorScreenPos();
    float step = width / strlen(text);
    for (int i = 0; i < strlen(text); i++) {
        float t = (float)i / (float)strlen(text);
        ImU32 col = ImGui::GetColorU32(ImVec4(
            accentColor.x * (1.0f - t) + 1.0f * t,
            accentColor.y * (1.0f - t) + 1.0f * t,
            accentColor.z * (1.0f - t) + 1.0f * t,
            1.0f));
        char c[2] = { text[i], 0 };
        draw->AddText(ImVec2(pos.x + i * step, pos.y), col, c);
    }
}

void SetVisual() {
    ImVec2 center = ImGui::GetMainViewport()->GetCenter();
    if (firstOpen) {
        windowPos = ImVec2(center.x - windowSize.x * 0.5f, center.y - windowSize.y * 0.5f);
        firstOpen = false;
        ApplyCustomStyle();
    }

    ImGui::SetNextWindowPos(windowPos, ImGuiCond_Always);
    ImGui::SetNextWindowSize(isCollapsed ? ImVec2(220, 50) : ImVec2(windowSize.x * menuScale, windowSize.y * menuScale));

    ImGui::Begin("ZU_UI", nullptr,
        ImGuiWindowFlags_NoCollapse |
        ImGuiWindowFlags_NoResize |
        ImGuiWindowFlags_NoTitleBar |
        ImGuiWindowFlags_NoScrollbar);
    windowPos = ImGui::GetWindowPos();
    ImGui::SetCursorPos(ImVec2(0, 0));
    ImGui::InvisibleButton("DragZone", ImVec2(ImGui::GetWindowWidth(), 40));
    if (ImGui::IsItemActive() && ImGui::IsMouseDragging(0)) {
        windowPos.x += ImGui::GetIO().MouseDelta.x;
        windowPos.y += ImGui::GetIO().MouseDelta.y;
        ImGui::SetWindowPos(windowPos);
    }

    ImGui::SetCursorPos(ImVec2(20, 20));
    DrawNeonText("ZU Virtual | NEON PANEL", 200);
    ImGui::SetCursorPos(ImVec2(20, 20));
    if (ImGui::InvisibleButton("Toggle", ImVec2(180, 30))) {
        isCollapsed = !isCollapsed;
    }

    if (!isCollapsed) {
        ImGui::SetCursorPosY(70);
        float centerTabs = (ImGui::GetWindowWidth() - 600) * 0.5f;
        ImGui::SetCursorPosX(centerTabs > 0 ? centerTabs : 20);
        if (ImGui::BeginTabBar("MainTabs")) {
            if (ImGui::BeginTabItem("ESP")) {
                ImGui::Checkbox("Enable ESP", &isESP);
                ImGui::Checkbox("Draw Lines", &isESPLine);
                ImGui::EndTabItem();
            }
            if (ImGui::BeginTabItem("Visual")) {
                ImGui::Checkbox("Crosshair", &enableCrosshair);
                ImGui::EndTabItem();
            }
            if (ImGui::BeginTabItem("Aimbot")) {
                ImGui::Checkbox("Enable Aimbot", &enableAimbot);
                ImGui::SliderFloat("Aimbot FOV", &aimbotFov, 0.0f, 180.0f);
                ImGui::Checkbox("Draw FOV", &aimbotDrawFov);
                ImGui::EndTabItem();
            }
            if (ImGui::BeginTabItem("Settings")) {
                ImGui::SliderFloat("Menu Scale", &menuScale, 0.8f, 1.5f);
                ImGui::ColorEdit4("Accent Color", (float*)&accentColor);
                ImGui::EndTabItem();
            }
            ImGui::EndTabBar();
        }
    }

    ImGui::End();
}