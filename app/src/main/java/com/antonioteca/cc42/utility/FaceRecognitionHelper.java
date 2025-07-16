package com.antonioteca.cc42.utility;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

public class FaceRecognitionHelper {

    static {
        System.loadLibrary("face_matcher");
    }

    // Chamada nativa
    public static native int matchSIMD(float[] embedding1, float[][] embedding2, float threshold);

    private final Interpreter interpreter; // Interpretador do TensorFlow Lite

    public FaceRecognitionHelper(Context context) throws IOException {
        MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, "mobilefacenet.tflite");
        interpreter = new Interpreter(modelBuffer);
    }

    // Método para obter o embedding (vectores de características) de uma face
    public float[] getFaceEmbedding(Bitmap faceBitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(faceBitmap, 112, 112, true); // Redimensionar a imagem para o tamanho esperado pelo modelo
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(112 * 112 * 3 * 4); // Buffer para armazenar os dados de entrada
        inputBuffer.order(ByteOrder.nativeOrder()); // Definir a ordem dos bytes na CPU

        int[] intValues = new int[112 * 112]; // Array para armazenar os valores RGB dos pixels
        resized.getPixels(intValues, 0, 112, 0, 0, 112, 112); // Obter os valores RGB dos pixels da imagem redimensionada

        // Preencher o buffer com os valores RGB normalizados
        for (int val : intValues) {
            inputBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
            inputBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
            inputBuffer.putFloat((val & 0xFF) / 255.0f);
        }

        float[][] embedding = new float[1][192]; // Buffer para armazenar o embedding (vetores de características)
        interpreter.run(inputBuffer, embedding); // Executar a inferência no modelo TensorFlow Lite
        return embedding[0]; // Retornar o embedding (vetor de características)
    }
}
