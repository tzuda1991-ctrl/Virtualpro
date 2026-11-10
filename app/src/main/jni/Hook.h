#include "Function.h"

void Hook() {
    APIHook();
    sleep(1);
    Application$$OpenURL = (void (*)(MonoString*)) FindMethod("UnityEngine.CoreModule.dll", "UnityEngine", "Application", "OpenURL", 1);
    get_main              = (void* (*)()) FindMethod("UnityEngine.dll", "UnityEngine", "Camera", "get_main", 0);
    WorldToScreen         = (Vector3 (*)(void*, Vector3)) FindMethod("UnityEngine.dll", "UnityEngine", "Camera", "WorldToScreenPoint", 1);
    get_transform         = (void* (*)(void*)) FindMethod("UnityEngine.dll", "UnityEngine", "Component", "get_transform", 0);
    get_position          = (Vector3 (*)(void*)) FindMethod("UnityEngine.dll", "UnityEngine", "Transform", "get_position", 0);
    SetResolution         = (void (*)(int, int, bool)) FindMethod("UnityEngine.dll", "UnityEngine", "Screen", "SetResolution", 0);

DobbyHook(
    FindMethod("Assembly-CSharp.dll", "", "PlayerRenderer", "LateUpdate", 0),
    (void*)Player_Update,
    (void**)&old_Player_Update
);

DobbyHook(
    FindMethod("Assembly-CSharp.dll", "", "PlayerRenderer", "OnDestroy", 0),
    (void*)Player_OnDestroy,
    (void**)&old_Player_OnDestroy
);
    sleep(5);
    InitHook = true;
}


