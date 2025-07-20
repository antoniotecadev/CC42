////
//// Created by anton on 16/07/2025.
////
//
//#include <jni.h>
//#include <arm_neon.h>
//#include <cmath>
//#include <vector>
//#include <arm_vector_types.h>
//
//float cosine_similarity_neon(const float* a, const float* b, int len) {
//    float32x4_t sum_ab = vmovq_n_f32(0.0f);
//    float32x4_t sum_aa = vmovq_n_f32(0.0f);
//    float32x4_t sum_bb = vmovq_n_f32(0.0f);
//
//    int i = 0;
//    for (; i + 4 <= len; i += 4) {
//        float32x4_t va = vld1q_f32(a + i);
//        float32x4_t vb = vld1q_f32(b + i);
//
//        sum_ab = vmlaq_f32(sum_ab, va, vb); // a * b
//        sum_aa = vmlaq_f32(sum_aa, va, va); // a * a
//        sum_bb = vmlaq_f32(sum_bb, vb, vb); // b * b
//    }
//
//    float ab[4], aa[4], bb[4];
//    vst1q_f32(ab, sum_ab);
//    vst1q_f32(aa, sum_aa);
//    vst1q_f32(bb, sum_bb);
//
//    float dot = ab[0] + ab[1] + ab[2] + ab[3];
//    float norm_a = aa[0] + aa[1] + aa[2] + aa[3];
//    float norm_b = bb[0] + bb[1] + bb[2] + bb[3];
//
//    for (; i < len; i++) {
//        dot += a[i] * b[i];
//        norm_a += a[i] * a[i];
//        norm_b += b[i] * b[i];
//    }
//
//    if (norm_a < 1e-6f || norm_b < 1e-6f) return -1.0f;
//    return dot / (std::sqrt(norm_a) * std::sqrt(norm_b));
//}
//
//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_antonioteca_cc42_utility_FaceRecognitionHelper_matchSIMD(
//        JNIEnv *env,
//        jclass clazz,
//        jfloatArray currentVector,
//        jobjectArray knownVectors,
//        jfloat threshold
//) {
//    int dim = env->GetArrayLength(currentVector);
//    std::vector<float> current(dim);
//    env->GetFloatArrayRegion(currentVector, 0, dim, current.data());
//
//    int knownSize = env->GetArrayLength(knownVectors);
//    std::vector<float> known(dim);
//    float bestSim = -1.0f;
//    int bestIndex = -1;
//
//    for (int i = 0; i < knownSize; i++) {
//        jfloatArray vecArray = (jfloatArray) env->GetObjectArrayElement(knownVectors, i);
//        int len = env->GetArrayLength(vecArray);
//        if (len != dim) {
//            env->DeleteLocalRef(vecArray);
//            continue;
//        }
//
//        env->GetFloatArrayRegion(vecArray, 0, dim, known.data());
//
//        float sim = cosine_similarity_neon(current.data(), known.data(), dim);
//
//        if (sim > bestSim) {
//            bestSim = sim;
//            bestIndex = i;
//        }
//
//        env->DeleteLocalRef(vecArray);
//    }
//
//    return (bestSim >= threshold) ? bestIndex : -1;
//}