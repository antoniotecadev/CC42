package com.antonioteca.cc42.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.antonioteca.cc42.R;

import java.io.File;

public class PdfSharer {
    public static void sharePdf(Context context, File pdfFile) {
        if (!pdfFile.exists()) {
            Util.showAlertDialogBuild("PDFSharer", context.getString(R.string.msg_file_pdf_not_found) + pdfFile.getAbsolutePath(), context, null);
            return;
        }
//        Criar um URI para arquivo PDF usando FileProvider (para segurança)
        Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);

//        Criar um Intent para compartilhar
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, pdfUri); // Anexar pdf
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Conceder permissão de leitura

//        Exibir a caixa de dialogo de compartilhamento
        context.startActivity(Intent.createChooser(intent, "Attendance List"));
    }
}
