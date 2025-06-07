package com.antonioteca.cc42.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.antonioteca.cc42.R;

import java.io.File;

public class PdfSharer {
    public static void sharePdf(Context context, File file, String typeApplication, String title) {
        if (!file.exists()) {
            Util.showAlertDialogBuild(context.getString(R.string.err), title, context, null);
            return;
        }
//        Criar um URI para arquivo Ficheiro usando FileProvider (para segurança)
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

//        Criar um Intent para compartilhar
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setDataAndType(fileUri, typeApplication);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri); // Anexar file
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Conceder permissão de leitura

//        Exibir a caixa de dialogo de compartilhamento
        context.startActivity(Intent.createChooser(intent, title));
    }
}
