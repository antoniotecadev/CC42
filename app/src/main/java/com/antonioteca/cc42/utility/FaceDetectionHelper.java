//package com.antonioteca.cc42.utility;
//
//import android.annotation.SuppressLint;
//import android.graphics.Bitmap;
//import android.graphics.Matrix;
//import android.media.Image;
//
//import androidx.annotation.NonNull;
//import androidx.camera.core.ImageProxy;
//
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.face.Face;
//import com.google.mlkit.vision.face.FaceDetection;
//import com.google.mlkit.vision.face.FaceDetector;
//import com.google.mlkit.vision.face.FaceDetectorOptions;
//
//import java.nio.ByteBuffer;
//
//public class FaceDetectionHelper {
//
//    // Initialize native methods
//    static {
//        System.loadLibrary("yuv_utils");
//    }
//
//    // Chamada nativa
//    public static native Bitmap convertYUVToBitmap(byte[] y, byte[] u, byte[] v, int width, int height);
//
//    public interface OnFaceCroppedListener {
//        void onFaceCropped(Bitmap faceBitmap);
//
//        void onFailure(Exception e);
//    }
//
//    Exception exception = new Exception("Nenhum rosto detectado");
//
//    public void detectAndCropFace(@NonNull ImageProxy imageProxy, @NonNull OnFaceCroppedListener listener) {
//
//        @SuppressLint("UnsafeOptInUsageError")
//        Image mediaImage = imageProxy.getImage(); // Obter a imagem do ImageProxy
//
//        // Preparar a imagem para o processamento
//        InputImage image;
//        if (mediaImage != null) {
//            image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
//        } else {
//            imageProxy.close();
//            return;
//        }
//
//        // Configurar o detector de rosto
//        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
//                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // Modo de desempenho
//                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE) // Modo de marcação
//                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE) // Modo de classificação
//                .build();
//
//        // Criar o detector de rosto
//        FaceDetector detector = FaceDetection.getClient(options);
//
//        // Detectar e recortar o rosto
//        detector.process(image)
//                .addOnSuccessListener(faces -> {
//                    if (faces.size() == 1) {
//                        Face face = faces.get(0); // Supondo que apenas um rosto seja detectado
////                      Rect bounds = face.getBoundingBox(); // Obter as coordenadas do rosto
//                        float headEulerAngleY = face.getHeadEulerAngleY(); // Obter o ângulo de inclinação lateral da cabeça (girar para a esquerda ou direita)
//                        float headEulerAngleZ = face.getHeadEulerAngleZ(); // Obter o ângulo de rotação da cabeça (inclinar a cabeça no ombro)
//                        Float smilingProbability = face.getSmilingProbability(); // Obter a probabilidade de estar sorrindo
//                        Float leftEyeOpenProbability = face.getLeftEyeOpenProbability(); // Obter a probabilidade de estar com os olhos abertos
//                        Float rightEyeOpenProbability = face.getRightEyeOpenProbability(); // Obter a probabilidade de estar com os olhos abertos
//
//                        if (headEulerAngleY < -15f || headEulerAngleY > 15f) {
//                            listener.onFailure(new Exception("Rosto não está na vertical"));
//                            return;
//                        } else if (headEulerAngleZ < -10f || headEulerAngleZ > 10f) {
//                            listener.onFailure(new Exception("Rosto não está na horizontal"));
//                            return;
//                        } else if (smilingProbability != null && smilingProbability > 0.5f) {
//                            listener.onFailure(new Exception("Rosto está sorrindo"));
//                            return;
//                        } else if ((leftEyeOpenProbability != null && leftEyeOpenProbability < 0.5f) || (rightEyeOpenProbability != null && rightEyeOpenProbability < 0.5f)) {
//                            listener.onFailure(new Exception("Rosto não está com os olhos abertos"));
//                            return;
//                        }
//
//                        Bitmap bitmap = convertImageProxyToBitmap(imageProxy); // Converter a imagem para Bitmap
//                        listener.onFaceCropped(rotationBitmap(bitmap, imageProxy.getImageInfo().getRotationDegrees()));
//                    } else {
//                        listener.onFailure(exception);
//                    }
//                })
//                .addOnFailureListener(listener::onFailure);
//    }
//
//    private Bitmap rotationBitmap(Bitmap bitmap, int rotationDegrees) {
//        // Criar uma matriz de transformação
//        Matrix matrix = new Matrix();
//        matrix.postRotate(rotationDegrees); // Rotacionar a imagem em 90 graus
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }
//
//    public Bitmap convertImageProxyToBitmap(@NonNull ImageProxy imageProxy) {
//        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes(); // Acessar os planos da imagem
//
//        ByteBuffer yBuffer = planes[0].getBuffer(); // Y
//        ByteBuffer uBuffer = planes[1].getBuffer(); // U
//        ByteBuffer vBuffer = planes[2].getBuffer(); // V
//
//        // Calcular os tamanhos
//        int ySize = yBuffer.remaining();
//        int uSize = uBuffer.remaining();
//        int vSize = vBuffer.remaining();
//
//        // Criar os arrays de bytes
//        byte[] y = new byte[ySize];
//        byte[] u = new byte[uSize];
//        byte[] v = new byte[vSize];
//
//        // Copiar os dados dos buffers para os arrays
//        yBuffer.get(y);
//        uBuffer.get(u);
//        vBuffer.get(v);
//
//        // Obter as dimensões da imagem
//        int width = imageProxy.getWidth();
//        int height = imageProxy.getHeight();
//
//        return convertYUVToBitmap(y, u, v, width, height); // Chamar a função nativa
//    }
//}
