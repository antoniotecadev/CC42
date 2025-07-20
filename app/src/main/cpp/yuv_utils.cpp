////
//// Created by anton on 13/07/2025.
////
//
//#include <jni.h>
//#include <android/bitmap.h>
//#include <cstring>
//#include <algorithm>
//
//// Conversão simples YUV420 → RGB (planar)
//static void yuv420ToRgb(int width, int height,
//                        const uint8_t *yData,
//                        const uint8_t *uData,
//                        const uint8_t *vData,
//                        uint32_t *outPixels) {
//
//    int uvRowStride = width / 2;
//    for (int y = 0; y < height; y++) {
//        for (int x = 0; x < width; x++) {
//            int Y = yData[y * width + x] & 0xFF;
//            int U = uData[(y / 2) * uvRowStride + (x / 2)] & 0xFF;
//            int V = vData[(y / 2) * uvRowStride + (x / 2)] & 0xFF;
//
//            int C = Y - 16;
//            int D = U - 128;
//            int E = V - 128;
//
//            int R = (298 * C + 409 * E + 128) >> 8;
//            int G = (298 * C - 100 * D - 208 * E + 128) >> 8;
//            int B = (298 * C + 516 * D + 128) >> 8;
//
//            R = std::min(std::max(R, 0), 255);
//            G = std::min(std::max(G, 0), 255);
//            B = std::min(std::max(B, 0), 255);
//
//            outPixels[y * width + x] =
//                    0xFF000000 | (R << 16) | (G << 8) | B;
//        }
//    }
//}
//
//// Chamada nativa
//extern "C" JNIEXPORT jobject JNICALL
//Java_com_antonioteca_cc42_utility_FaceDetectionHelper_convertYUVToBitmap(
//        JNIEnv *env,
//        jclass clazz,
//        jbyteArray yArr,
//        jbyteArray uArr,
//        jbyteArray vArr,
//        jint width,
//        jint height
//) {
//    jbyte *y = env->GetByteArrayElements(yArr, nullptr);
//    jbyte *u = env->GetByteArrayElements(uArr, nullptr);
//    jbyte *v = env->GetByteArrayElements(vArr, nullptr);
//
//    uint32_t *pixels = new uint32_t[width * height];
//
//    yuv420ToRgb(width, height,
//                reinterpret_cast<const uint8_t *>(y),
//                reinterpret_cast<const uint8_t *>(u),
//                reinterpret_cast<const uint8_t *>(v),
//                pixels);
//
//    env->ReleaseByteArrayElements(yArr, y, JNI_ABORT);
//    env->ReleaseByteArrayElements(uArr, u, JNI_ABORT);
//    env->ReleaseByteArrayElements(vArr, v, JNI_ABORT);
//
//    // Cria um Bitmap ARGB_8888 a partir dos pixels
//    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
//    jmethodID createBitmapMethod = env->GetStaticMethodID(
//            bitmapClass, "createBitmap",
//            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
//
//    jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
//    jfieldID argb8888Field = env->GetStaticFieldID(configClass, "ARGB_8888",
//                                                   "Landroid/graphics/Bitmap$Config;");
//    jobject config = env->GetStaticObjectField(configClass, argb8888Field);
//
//    jobject bitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod,
//                                                 width, height, config);
//
//    AndroidBitmapInfo info;
//    void *pixelsDst;
//    AndroidBitmap_getInfo(env, bitmap, &info);
//    AndroidBitmap_lockPixels(env, bitmap, &pixelsDst);
//    memcpy(pixelsDst, pixels, width * height * 4);
//    AndroidBitmap_unlockPixels(env, bitmap);
//
//    delete[] pixels;
//    return bitmap;
//}
