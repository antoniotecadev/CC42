package com.antonioteca.cc42.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.antonioteca.cc42.R;

import java.io.File;

public class PdfViewer {

    public static void openPdf(Context context, File pdfFile) {
        if (!pdfFile.exists()) {
            Util.showAlertDialogBuild("PDFViewer", context.getString(R.string.msg_file_pdf_not_found) + pdfFile.getAbsolutePath(), context, null);
            return;
        }
//        Criar um URI para arquivo PDF usando FileProvider (para segurança)
        Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);

//        Criar um Intent para visualizar o PDF
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Conceder permissão de leitura

//        Verificar se há um aplicativo disponível para abrir o PDF
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Util.showAlertDialogBuild("PDFViewer", context.getString(R.string.msg_no_pdf_viewing_applications_were_found) + pdfFile.getAbsolutePath(), context, null);
        }
    }
}
