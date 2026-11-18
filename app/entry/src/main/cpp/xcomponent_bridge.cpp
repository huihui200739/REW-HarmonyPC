#include <napi/native_api.h>
#include <ace/xcomponent/native_interface_xcomponent.h>
#include <native_window/external_window.h>
#include <sys/mman.h>
#include <cstring>

namespace {

OHNativeWindow* g_window = nullptr;
int g_width = 1600;
int g_height = 1000;

void FillSolid(uint32_t argb) {
    if (g_window == nullptr) {
        return;
    }
    OHNativeWindowBuffer* buffer = nullptr;
    int fenceFd = -1;
    if (OH_NativeWindow_NativeWindowRequestBuffer(g_window, &buffer, &fenceFd) != 0) {
        return;
    }
    OH_NativeWindow_NativeWindowHandleOpt(g_window, SET_BUFFER_GEOMETRY, g_width, g_height);
    BufferHandle* handle = OH_NativeWindow_GetBufferHandleFromNative(buffer);
    if (handle == nullptr) {
        OH_NativeWindow_NativeWindowAbortBuffer(g_window, buffer);
        return;
    }
    void* mapped = mmap(handle->virAddr, handle->size, PROT_READ | PROT_WRITE, MAP_SHARED, handle->fd, 0);
    if (mapped != MAP_FAILED) {
        uint32_t* px = static_cast<uint32_t*>(mapped);
        size_t count = static_cast<size_t>(handle->size) / sizeof(uint32_t);
        for (size_t i = 0; i < count; i++) {
            px[i] = argb;
        }
        munmap(mapped, handle->size);
    }
    Region flushRegion = {nullptr, 0};
    OH_NativeWindow_NativeWindowFlushBuffer(g_window, buffer, fenceFd, flushRegion);
}

void OnSurfaceCreatedCB(OH_NativeXComponent* component, void* window) {
    g_window = static_cast<OHNativeWindow*>(window);
}

void OnSurfaceChangedCB(OH_NativeXComponent* component, void* window) {
    g_window = static_cast<OHNativeWindow*>(window);
    uint64_t w = 0;
    uint64_t h = 0;
    OH_NativeXComponent_GetXComponentSize(component, window, &w, &h);
    if (w > 0 && h > 0) {
        g_width = static_cast<int>(w);
        g_height = static_cast<int>(h);
    }
    FillSolid(0xFF2E7D32);
}

void OnSurfaceDestroyedCB(OH_NativeXComponent* component, void* window) {
    g_window = nullptr;
}

void DispatchTouchEventCB(OH_NativeXComponent* component, void* window) {
}

OH_NativeXComponent_Callback g_callback = {
    OnSurfaceCreatedCB,
    OnSurfaceChangedCB,
    OnSurfaceDestroyedCB,
    DispatchTouchEventCB,
};

}  // namespace

void RegisterXComponent(napi_env env, napi_value exports) {
    napi_value exportInstance = nullptr;
    if (napi_get_named_property(env, exports, OH_NATIVE_XCOMPONENT_OBJ, &exportInstance) != napi_ok) {
        return;
    }
    OH_NativeXComponent* component = nullptr;
    if (napi_unwrap(env, exportInstance, reinterpret_cast<void**>(&component)) != napi_ok) {
        return;
    }
    OH_NativeXComponent_RegisterCallback(component, &g_callback);
}
