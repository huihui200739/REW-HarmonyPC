#include <jni.h>
#include <ohaudio/native_audiostream_base.h>
#include <ohaudio/native_audiostreambuilder.h>
#include <ohaudio/native_audiorenderer.h>
#include <ohaudio/native_audiocapturer.h>
#include <hilog/log.h>
#include <mutex>
#include <cstring>
#include <cstdlib>

extern "C" {

namespace {

constexpr int kRingCapacity = 1 << 18;   // 256 KB, ~0.7 s @48k/16bit/stereo

struct RingBuffer {
    unsigned char data[kRingCapacity];
    volatile size_t readPos = 0;
    volatile size_t writePos = 0;
    std::mutex lock;
};

struct RendererState {
    OH_AudioRenderer* renderer = nullptr;
    RingBuffer* ring = nullptr;
    bool started = false;
};

RendererState g_state;

struct CaptureState {
    OH_AudioCapturer* capturer = nullptr;
    RingBuffer* ring = nullptr;
    bool started = false;
};

CaptureState g_capture;

size_t ringReadable(RingBuffer* rb) {
    size_t w = rb->writePos;
    size_t r = rb->readPos;
    return (w >= r) ? (w - r) : (kRingCapacity - r + w);
}

void ringWrite(RingBuffer* rb, const unsigned char* src, size_t len) {
    std::lock_guard<std::mutex> guard(rb->lock);
    size_t space = kRingCapacity - ringReadable(rb) - 1;
    if (len > space) {
        len = space;
    }
    size_t w = rb->writePos;
    size_t first = kRingCapacity - w;
    if (first > len) {
        first = len;
    }
    memcpy(rb->data + w, src, first);
    if (len > first) {
        memcpy(rb->data, src + first, len - first);
    }
    rb->writePos = (w + len) % kRingCapacity;
}

size_t ringRead(RingBuffer* rb, unsigned char* dst, size_t len) {
    std::lock_guard<std::mutex> guard(rb->lock);
    size_t avail = ringReadable(rb);
    if (len > avail) {
        len = avail;
    }
    size_t r = rb->readPos;
    size_t first = kRingCapacity - r;
    if (first > len) {
        first = len;
    }
    memcpy(dst, rb->data + r, first);
    if (len > first) {
        memcpy(dst, rb->data, len - first);
    }
    rb->readPos = (r + len) % kRingCapacity;
    return len;
}

OH_AudioData_Callback_Result OnWriteData(OH_AudioRenderer* renderer, void* userData,
                                         void* audioData, int32_t audioDataSize) {
    RingBuffer* rb = static_cast<RingBuffer*>(userData);
    size_t got = ringRead(rb, static_cast<unsigned char*>(audioData),
                          static_cast<size_t>(audioDataSize));
    if (got < static_cast<size_t>(audioDataSize)) {
        memset(static_cast<unsigned char*>(audioData) + got, 0,
               static_cast<size_t>(audioDataSize) - got);
    }
    return AUDIO_DATA_CALLBACK_RESULT_VALID;
}

void OnReadData(OH_AudioCapturer* capturer, void* userData,
                void* audioData, int32_t audioDataSize) {
    RingBuffer* rb = static_cast<RingBuffer*>(userData);
    ringWrite(rb, static_cast<const unsigned char*>(audioData),
              static_cast<size_t>(audioDataSize));
}

void OnError(OH_AudioRenderer* renderer, void* userData, OH_AudioStream_Result error) {
    OH_LOG_Print(LOG_APP, LOG_ERROR, 0, "REWAUDIO", "renderer error=%{public}d",
                 static_cast<int>(error));
}

}  // namespace

JNIEXPORT jlong JNICALL
Java_ohos_sound_OhosSourceDataLine_nativeOpen(JNIEnv* env, jclass clazz,
                                              jint sampleRate, jint channels, jint bits) {
    if (g_state.renderer != nullptr) {
        return reinterpret_cast<jlong>(&g_state);
    }
    g_state.ring = new RingBuffer();

    OH_AudioStreamBuilder* builder = nullptr;
    if (OH_AudioStreamBuilder_Create(&builder, AUDIOSTREAM_TYPE_RENDERER) != AUDIOSTREAM_SUCCESS) {
        OH_LOG_Print(LOG_APP, LOG_ERROR, 0, "REWAUDIO", "builder create failed");
        return 0;
    }
    OH_AudioStreamBuilder_SetSamplingRate(builder, sampleRate);
    OH_AudioStreamBuilder_SetChannelCount(builder, channels);
    OH_AudioStreamBuilder_SetSampleFormat(builder,
            bits == 8 ? AUDIOSTREAM_SAMPLE_U8 : AUDIOSTREAM_SAMPLE_S16LE);
    OH_AudioStreamBuilder_SetEncodingType(builder, AUDIOSTREAM_ENCODING_TYPE_RAW);
    OH_AudioStreamBuilder_SetRendererInfo(builder, AUDIOSTREAM_USAGE_MUSIC);
    OH_AudioStreamBuilder_SetRendererWriteDataCallback(builder, OnWriteData, g_state.ring);
    OH_AudioStreamBuilder_SetRendererErrorCallback(builder, OnError, nullptr);

    if (OH_AudioStreamBuilder_GenerateRenderer(builder, &g_state.renderer) != AUDIOSTREAM_SUCCESS) {
        OH_LOG_Print(LOG_APP, LOG_ERROR, 0, "REWAUDIO", "generate renderer failed");
        OH_AudioStreamBuilder_Destroy(builder);
        return 0;
    }
    OH_AudioStreamBuilder_Destroy(builder);
    OH_AudioRenderer_Start(g_state.renderer);
    g_state.started = true;
    OH_LOG_Print(LOG_APP, LOG_INFO, 0, "REWAUDIO",
                 "renderer started %{public}d Hz %{public}d ch %{public}d bit",
                 sampleRate, channels, bits);
    return reinterpret_cast<jlong>(&g_state);
}

JNIEXPORT jint JNICALL
Java_ohos_sound_OhosSourceDataLine_nativeWrite(JNIEnv* env, jclass clazz, jlong handle,
                                               jbyteArray data, jint off, jint len) {
    RendererState* st = reinterpret_cast<RendererState*>(handle);
    if (st == nullptr || st->ring == nullptr || data == nullptr) {
        return len;
    }
    jbyte* buf = env->GetByteArrayElements(data, nullptr);
    if (buf == nullptr) {
        return len;
    }
    ringWrite(st->ring, reinterpret_cast<const unsigned char*>(buf) + off,
              static_cast<size_t>(len));
    env->ReleaseByteArrayElements(data, buf, JNI_ABORT);
    return len;
}

JNIEXPORT void JNICALL
Java_ohos_sound_OhosSourceDataLine_nativeClose(JNIEnv* env, jclass clazz, jlong handle) {
    RendererState* st = reinterpret_cast<RendererState*>(handle);
    if (st != nullptr && st->renderer != nullptr) {
        OH_AudioRenderer_Stop(st->renderer);
        OH_AudioRenderer_Release(st->renderer);
        st->renderer = nullptr;
        st->started = false;
        delete st->ring;
        st->ring = nullptr;
        OH_LOG_Print(LOG_APP, LOG_INFO, 0, "REWAUDIO", "renderer closed");
    }
}

JNIEXPORT jlong JNICALL
Java_ohos_sound_OhosTargetDataLine_nativeOpen(JNIEnv* env, jclass clazz,
                                              jint sampleRate, jint channels, jint bits) {
    if (g_capture.capturer != nullptr) {
        return reinterpret_cast<jlong>(&g_capture);
    }
    g_capture.ring = new RingBuffer();

    OH_AudioStreamBuilder* builder = nullptr;
    if (OH_AudioStreamBuilder_Create(&builder, AUDIOSTREAM_TYPE_CAPTURER) != AUDIOSTREAM_SUCCESS) {
        OH_LOG_Print(LOG_APP, LOG_ERROR, 0, "REWAUDIO", "capturer builder create failed");
        return 0;
    }
    OH_AudioStreamBuilder_SetSamplingRate(builder, sampleRate);
    OH_AudioStreamBuilder_SetChannelCount(builder, channels);
    OH_AudioStreamBuilder_SetSampleFormat(builder,
            bits == 8 ? AUDIOSTREAM_SAMPLE_U8 : AUDIOSTREAM_SAMPLE_S16LE);
    OH_AudioStreamBuilder_SetEncodingType(builder, AUDIOSTREAM_ENCODING_TYPE_RAW);
    OH_AudioStreamBuilder_SetCapturerInfo(builder, AUDIOSTREAM_SOURCE_TYPE_MIC);
    OH_AudioStreamBuilder_SetCapturerReadDataCallback(builder, OnReadData, g_capture.ring);

    if (OH_AudioStreamBuilder_GenerateCapturer(builder, &g_capture.capturer) != AUDIOSTREAM_SUCCESS) {
        OH_LOG_Print(LOG_APP, LOG_ERROR, 0, "REWAUDIO", "generate capturer failed");
        OH_AudioStreamBuilder_Destroy(builder);
        return 0;
    }
    OH_AudioStreamBuilder_Destroy(builder);
    OH_AudioCapturer_Start(g_capture.capturer);
    g_capture.started = true;
    OH_LOG_Print(LOG_APP, LOG_INFO, 0, "REWAUDIO",
                 "capturer started %{public}d Hz %{public}d ch", sampleRate, channels);
    return reinterpret_cast<jlong>(&g_capture);
}

JNIEXPORT jint JNICALL
Java_ohos_sound_OhosTargetDataLine_nativeRead(JNIEnv* env, jclass clazz, jlong handle,
                                              jbyteArray data, jint off, jint len) {
    CaptureState* st = reinterpret_cast<CaptureState*>(handle);
    if (st == nullptr || st->ring == nullptr || data == nullptr) {
        return 0;
    }
    jbyte* buf = env->GetByteArrayElements(data, nullptr);
    if (buf == nullptr) {
        return 0;
    }
    size_t got = ringRead(st->ring, reinterpret_cast<unsigned char*>(buf) + off,
                          static_cast<size_t>(len));
    env->ReleaseByteArrayElements(data, buf, 0);
    return static_cast<jint>(got);
}

JNIEXPORT void JNICALL
Java_ohos_sound_OhosTargetDataLine_nativeClose(JNIEnv* env, jclass clazz, jlong handle) {
    CaptureState* st = reinterpret_cast<CaptureState*>(handle);
    if (st != nullptr && st->capturer != nullptr) {
        OH_AudioCapturer_Stop(st->capturer);
        OH_AudioCapturer_Release(st->capturer);
        st->capturer = nullptr;
        st->started = false;
        delete st->ring;
        st->ring = nullptr;
        OH_LOG_Print(LOG_APP, LOG_INFO, 0, "REWAUDIO", "capturer closed");
    }
}

}  // extern "C"
