package com.antonioteca.cc42.utility;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CsvExporter {

    private static final String CSV_HEADER = "Nº;Nome completo;Login;Presença\n"; // Adapte os cabeçalhos
    private static final String UTF8_BOM = "\uFEFF"; // detectar que o arquivo está codificado em UTF-8

    public interface ExportCallback {
        void onSuccess(File file);

        void onError(String error);
    }

    public static void exportUsersToCsv(Context context, List<User> users, String baseFileName, String eventName, String eventDate, ExportCallback callback) {
        // Usar um ExecutorService para rodar em background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Background work here
            if (users == null || users.isEmpty()) {
                handler.post(() -> callback.onError(context.getString(R.string.msg_attendance_list_empty)));
                return;
            }

            StringBuilder csvData = new StringBuilder();
            csvData.append(UTF8_BOM);
            csvData.append(";").append(eventName).append(";").append(eventDate).append(";").append("\n\n");
            csvData.append(CSV_HEADER);
            int i = 1;
            for (User user : users) {
                csvData.append(i).append(";");
                csvData.append(escapeCsvString(user.displayName)).append(";");  // Use sua classe User
                csvData.append(escapeCsvString(user.login)).append(";"); // Use sua classe User
                csvData.append(user.isPresent() ? context.getString(R.string.text_present) : context.getString(R.string.text_absent));            // Use sua classe User
                csvData.append("\n");
                i++;
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = baseFileName + "_" + timeStamp + ".csv";
            File fileSaved;

            try {
                byte[] csvBytes = csvData.toString().getBytes(StandardCharsets.UTF_8); // Usar StandardCharsets.UTF_8
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = context.getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + context.getString(R.string.app_name)); // Salva em Downloads/SeuAppName

                    Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    Uri itemUri = resolver.insert(collection, contentValues);

                    if (itemUri != null) {
                        try (OutputStream os = resolver.openOutputStream(itemUri)) {
                            if (os != null) {
                                os.write(csvBytes);
                                String filePathSaved = Environment.DIRECTORY_DOWNLOADS + File.separator + context.getString(R.string.app_name) + File.separator + fileName; // Caminho aproximado para exibição
                                fileSaved = new File(filePathSaved);
                            } else {
                                throw new IOException("Failed to get output stream.");
                            }
                        }
                    } else {
                        throw new IOException("Failed to create new MediaStore record.");
                    }
                } else {
                    // Para Android < Q (API < 29) - Requer permissão WRITE_EXTERNAL_STORAGE
                    // Certifique-se que a permissão foi concedida antes de chamar este método
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File appDir = new File(downloadsDir, context.getString(R.string.app_name));
                    if (!appDir.exists()) {
                        final boolean mkdirs = appDir.mkdirs();
                        if (!mkdirs) {
                            throw new IOException("Failed to create directory.");
                        }
                    }
                    File file = new File(appDir, fileName);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(csvBytes);
                        fileSaved = file;
                    }
                }
                File finalFileSaved = fileSaved;
                handler.post(() -> callback.onSuccess(finalFileSaved));
            } catch (UnsupportedEncodingException e) {
                handler.post(() -> callback.onError("Erro de codificação: " + e.getMessage()));
            } catch (IOException e) {
                handler.post(() -> callback.onError("Erro ao salvar arquivo CSV: " + e.getMessage()));
            }
        });
    }

    // Helper para escapar vírgulas e aspas em strings para CSV
    private static String escapeCsvString(String data) {
        if (data == null) return "";
        String escapedData = data.replaceAll("\"", "\"\"");
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}