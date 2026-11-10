bool InitHook = false;

struct Variable {
    bool Esp = false;
    bool ESPLine = false;
    bool ESPBox = false;
    bool ESPDistance = false;
    bool ESPHeal = false;
    bool ESPName = false;
    bool ESPSkeleton = false;
    bool ESPObject = false;
    bool ESP3DBox = false;
    bool ESP360 = false;
    
    float AimFov = 250.f;
    float ESPLineRotation = 0.f;
    float ThicknessLine = 3.f;
    float ESPBoxSize = 3.f;
    ImVec4 ESPLineColor = ImVec4(1.f, 0.f, 0.f, 1.f);
    ImVec4 ESPBoxColor = ImVec4(1.f, 0.f, 0.f, 1.f);
    ImVec4 ESPObjectColor = ImVec4(255.f, 255.f, 0.f, 1.f);
    ImVec4 ESP3DBoxColor = ImVec4(1.f, 0.f, 0.f, 1.f);
    ImVec4 CrossFColor = ImVec4(0.f, 1.f, 0.f, 1.f);
    ImVec4 ESPDistanceColor = ImVec4(1.f, 1.f, 1.f, 1.f);
    ImVec4 ESPNameColor = ImVec4(1.f, 1.f, 1.f, 1.f);
    ImVec4 ESPHealColor = ImVec4(1.f, 1.f, 1.f, 1.f);
    ImVec4 ESPSkeletColor = ImVec4(1.f, 0.f, 0.f, 0.6f);
    ImVec4 ESPOnlineColor = ImVec4(1.f, 1.f, 1.f, 1.f);
    
} var;

static int tabM = 1;
bool tabM1 = true;
bool tabM2 = false;
bool tabM3 = false;
bool tabM4 = false;