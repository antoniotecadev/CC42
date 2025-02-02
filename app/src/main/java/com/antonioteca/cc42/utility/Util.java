package com.antonioteca.cc42.utility;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ImageQrCodeBinding;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Objects;

import io.noties.markwon.Markwon;

public class Util {

    public static void showAlertDialogBuild(String title, String message, Context context, Runnable runnableTryAgain) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        if (runnableTryAgain == null)
            builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        else {
            builder.setPositiveButton(R.string.try_again, (dialogInterface, i) -> runnableTryAgain.run());
            builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        }
        builder.show();
    }

    public static void showAlertDialogSynchronized(Context context, Runnable runnableTryAgain) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.msg_synchronization);
        builder.setIcon(R.drawable.logo_42);
        if (runnableTryAgain == null)
            builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        else {
            builder.setPositiveButton(R.string.synchronize, (dialogInterface, i) -> runnableTryAgain.run());
            builder.setNeutralButton(R.string.later, (dialogInterface, i) -> dialogInterface.dismiss());
        }
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
            bitmap = barcodeEncoder.encodeBitmap("cc42" + content, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            showAlertDialogBuild(context.getString(R.string.err), e.getMessage(), context, null);
        }
        return bitmap;
    }

    public static void showModalQrCode(Context context, Bitmap bitmapQrCode, String title, String description) {
        ImageQrCodeBinding binding = ImageQrCodeBinding.inflate(LayoutInflater.from(context));
        binding.textViewTitle.setText(title);
        binding.textViewDescription.setText(description);
        binding.imageViewQrCode.setImageBitmap(bitmapQrCode);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(binding.getRoot());
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        binding.closeModalButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void showModalUserDetails(Context context, String title, String description, String urlImageUserRegisteredEvent, boolean isPresent) {
        ImageQrCodeBinding binding = ImageQrCodeBinding.inflate(LayoutInflater.from(context));
        int color = isPresent ? Color.rgb(0, 200, 0) : Color.rgb(200, 0, 0);
        binding.textViewTitle.setText(title);
        binding.textViewDescription.setText(description);
        binding.closeModalButton.setBackgroundColor(color);
        binding.closeModalButton.setText(isPresent ? R.string.text_present : R.string.text_absent);
        Util.setImageUserRegistered(context, urlImageUserRegisteredEvent, binding.imageViewQrCode);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        binding.closeModalButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void setImageUserRegistered(Context context, String imageUrl, ImageView imageViewUserRegistered) {
        GlideApp.with(context)
                .load(imageUrl)
                .circleCrop() // Recorta a imagem para ser circular
                .placeholder(R.drawable.logo_42) // Imagem de substituição enquanto a imagem carrega
                .error(R.drawable.logo_42) // Imagem a ser mostrada caso ocorra um erro
                .into(imageViewUserRegistered);
    }

    public static void setVisibleProgressBar(ProgressBar progressBar, FloatingActionButton floatingActionButton, SharedViewModel sharedViewModel) {
        sharedViewModel.setDisabledRecyclerView(true);
        progressBar.setVisibility(View.VISIBLE);
        floatingActionButton.setVisibility(View.INVISIBLE);
    }

    public static void setInvisibleProgressBar(ProgressBar progressBar, FloatingActionButton floatingActionButton, SharedViewModel sharedViewModel) {
        progressBar.setVisibility(View.INVISIBLE);
        floatingActionButton.setVisibility(View.VISIBLE);
        sharedViewModel.setDisabledRecyclerView(false);
    }

    public static void showAlertDialogMessage(Context context,
                                              LayoutInflater layoutInflater,
                                              String title,
                                              String message,
                                              String colorString,
                                              Runnable runnableResumeCamera) {
        View customView = layoutInflater.inflate(R.layout.modal_layout_event_message, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(customView)
                .setCancelable(false);  // Impede que o modal seja fechado clicando fora
        TextView modalMessage = customView.findViewById(R.id.modalMessage);
        TextView modalTitle = customView.findViewById(R.id.modalTitle);
        Button closeModalButton = customView.findViewById(R.id.closeModalButton);
        int color = Color.parseColor(colorString);
        ColorStateList colorStateList = ColorStateList.valueOf(color);
        modalTitle.setText(title);
        modalMessage.setText(message);
        modalTitle.setTextColor(color);
        closeModalButton.setBackgroundTintList(colorStateList);
        AlertDialog dialog = builder.create();
        dialog.show();
        if (runnableResumeCamera == null) {
            closeModalButton.setOnClickListener(v -> dialog.dismiss());
        } else {
            closeModalButton.setOnClickListener(v -> {
                runnableResumeCamera.run();
                dialog.dismiss();
            });
            if (Objects.equals(title, context.getString(R.string.sucess))) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    runnableResumeCamera.run();
                    dialog.dismiss();
                }, 3000);
            }
        }
    }

    public static void startVibration(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            else
                vibrator.vibrate(200);
    }
}