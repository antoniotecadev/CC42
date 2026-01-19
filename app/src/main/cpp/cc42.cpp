//
// Created by antoniotecadev on 1/19/26.
//

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_antonioteca_cc42_utility_AESUtil_getSecretKeyFromJNI(
        JNIEnv* env,
        jclass clazz) {
    // Substitua pela sua chave original
    std::string secretKey = "SUA_CHAVE_AQUI";
    return env->NewStringUTF(secretKey.c_str());
}
