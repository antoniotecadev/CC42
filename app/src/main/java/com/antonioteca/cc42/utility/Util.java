package com.antonioteca.cc42.utility;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ImageQrCodeBinding;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import io.noties.markwon.Markwon;

public class Util {

    public static void showAlertDialogBuild(String title, String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    public static void setColorCoalition(ViewGroup viewGroup, String color) {
        if (color != null) {
            try {
                viewGroup.setBackgroundColor(Color.parseColor(color));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setFormattedText(TextView textView, String formattedText) {
        Spanned result;

        // Verifica a versão do Android para usar o método correto de Html.fromHtml
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(formattedText);
        }

        // Define o texto formatado no TextView
        textView.setText(result);
    }

    public static void setMarkdownText(TextView textView, String markdownText) {
        // Inicializa o Markwon
        Markwon markwon = Markwon.create(textView.getContext());

        // Renderiza o Markdown e define no TextView
        markwon.setMarkdown(textView, markdownText);
    }

    public static Bitmap generateQrCode(Context context, String content) {
        Bitmap bitmap = null;
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            showAlertDialogBuild(context.getString(R.string.err), e.getMessage(), context);
        }
        return bitmap;
    }

    public static void showAlertDialogQrCode(Context context, Bitmap bitmapQrCode, String eventName) {
        ImageQrCodeBinding binding = ImageQrCodeBinding.inflate(LayoutInflater.from(context));
        binding.textViewEventTitle.setText(eventName);
        binding.imageViewQrCode.setImageBitmap(bitmapQrCode);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.qr_code);
        builder.setView(binding.getRoot());
        builder.setPositiveButton(R.string.close, (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
