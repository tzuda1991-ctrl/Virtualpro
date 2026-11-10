#include "Utils.h"
#include "Draw.h"
#include "Variable.h"
#include "il2cpp.h"

int glWidth, glHeight;

//##############################################

bool isESP = true;
bool isESPLine = true;

std::vector<void*> players;
void (*Application$$OpenURL)(MonoString*);
void (*SetResolution)(int width, int height, bool fullscreen);
void* (*get_transform)(void* instance);
Vector3 (*get_position)(void* instance);
Vector3 (*WorldToScreen)(void* camera, Vector3 position);
void* (*get_main)();

void OpenURL(const char* a) {
    if (Application$$OpenURL)
        Application$$OpenURL(il2cpp_string_new(a));
}

Vector3 getPosition(void* transform) {
    return get_position(get_transform(transform));
}

bool playerFind(void* player) {
    if (player != NULL) {
        for (int i = 0; i < players.size(); i++) {
            if (player == players[i]) return true;
        }
    }
    return false;
}
//##############################################

const Il2CppImage* GetImageToAssembly(const char* assembly) {
    Il2CppDomain* domain = il2cpp_domain_get();
    const Il2CppAssembly* domain_asm = il2cpp_domain_assembly_open(domain, assembly);
    if (!domain_asm) return nullptr;
    return il2cpp_assembly_get_image(domain_asm);
}

const Il2CppClass* GetClass(const char* assembly, const char* namespaze, const char* name) {
    const Il2CppImage* asm_img = GetImageToAssembly(assembly);
    if (!asm_img) return nullptr;
    return il2cpp_class_from_name(asm_img, namespaze, name);
}

void* FindMethod(const std::string& name_assembly, const std::string& name_space, const std::string& type_name, const std::string& method_name, size_t args = 0) {
    const Il2CppImage* image = GetImageToAssembly(name_assembly.c_str());
    if (!image) return nullptr;

    const Il2CppClass* klass = il2cpp_class_from_name(image, name_space.c_str(), type_name.c_str());
    if (!klass) return nullptr;

    const MethodInfo* method = il2cpp_class_get_method_from_name(klass, method_name.c_str(), args);
    if (!method || !method->methodPointer) return nullptr;

    return (void*)method->methodPointer;
}

//##############################################