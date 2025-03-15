package com.antonioteca.cc42.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ImageQrCodeBinding;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.noties.markwon.Markwon;

public class Util {

    public static void setRequestPermissionLauncherNotification(Context context, int REQUEST_CODE_POST_NOTIFICATIONS) {
        // Verificar se a permissão foi concedida para exibir notificações - Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Util.showAlertDialogMessage(context, LayoutInflater.from(context), context.getString(R.string.notificatio_header), context.getString(R.string.notification_permission_message), "#4CAF50", () -> ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS));
            }
        }
    }

    public static void showAlertDialogBuild(String title, String message, Context context, Runnable runnableTryAgain) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(R.drawable.logo_42);
        if (runnableTryAgain == null)
            builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        else {
            builder.setNeutralButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
            builder.setPositiveButton(R.string.list_reload, (dialogInterface, i) -> runnableTryAgain.run());
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

    public static Bitmap generateQrCodeSimple(Context context, String content) {
        Bitmap bitmap = null;
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap("cc42" + content, BarcodeFormat.QR_CODE, 600, 600);
        } catch (Exception e) {
            showAlertDialogBuild(context.getString(R.string.err), e.getMessage(), context, null);
        }
        return bitmap;
    }

    public static Bitmap generateQrCodeWithLogo(Context context, String content) {
        try {
            // Configurar parâmetros do QR Code com alto nível de correção de erro
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Nível H
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1); // Margem mínima
            // Gerar QR code
            BitMatrix bitMatrix = new MultiFormatWriter().encode("cc42" + content, BarcodeFormat.QR_CODE, 600, 600, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrCode = barcodeEncoder.createBitmap(bitMatrix);
            // Criar bitmap para desenhar (com espaço central)
            // Combina o QR Code com o logotipo
            Bitmap combinedBitmap = Bitmap.createBitmap(qrCode.getWidth(), qrCode.getHeight(), qrCode.getConfig());
            Canvas canvas = new Canvas(combinedBitmap);
            canvas.drawBitmap(qrCode, 0, 0, null);
            // Obter o logotipo
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_42);
            if (logo != null) {
                // Criar espaço para o logotipo
                int logoSize = qrCode.getWidth() / 5;
                int centerX = (qrCode.getWidth() - logoSize) / 2;
                int centerY = (qrCode.getHeight() - logoSize) / 2;
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(centerX, centerY, centerX + logoSize, centerY + logoSize, paint);
                // Redimensionar logo
                Bitmap resizedLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, false);
                // Desenhar logo sobre o espaço
                canvas.drawBitmap(resizedLogo, centerX, centerY, null);
            }
            return combinedBitmap;
        } catch (Exception e) {
            showAlertDialogBuild(context.getString(R.string.err), e.getMessage(), context, null);
            return null;
        }
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

    public static void showModalUserDetails(Context context, String title, String description, String urlImageUserRegisteredEvent, String textButtom, boolean isPresent) {
        ImageQrCodeBinding binding = ImageQrCodeBinding.inflate(LayoutInflater.from(context));
        int color = isPresent ? ContextCompat.getColor(context, R.color.green) : ContextCompat.getColor(context, R.color.red);
        binding.textViewTitle.setText(title);
        binding.textViewDescription.setText(description);
        binding.closeModalButton.setBackgroundColor(color);
        binding.closeModalButton.setText(textButtom);
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
                .placeholder(R.drawable.ic_baseline_account_circle_300) // Imagem de substituição enquanto a imagem carrega
                .error(R.drawable.ic_baseline_account_circle_300) // Imagem a ser mostrada caso ocorra um erro
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

    private static void launchIntentPermission(boolean containsUri, Context context, ActivityResultLauncher<Intent> intentActivityResultLauncher) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        }
        if (containsUri) {
            Uri uri_ = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri_);
        }
        intentActivityResultLauncher.launch(intent);
    }

    private static void launchIntentPermission(Context context, ActivityResultLauncher<Intent> requestIntentPermission) {
        try {
            launchIntentPermission(true, context, requestIntentPermission);
        } catch (Exception e) {
            launchIntentPermission(false, null, requestIntentPermission);
        }
    }

    public static boolean launchPermissionDocument(Context context, ActivityResultLauncher<Intent> requestIntentPermission, ActivityResultLauncher<String> requestPermission, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager())
                return true;
            else
                launchIntentPermission(context, requestIntentPermission);
        } else
            requestPermission.launch(permission);
        return false;
    }

    public static void setAppLanguage(String languageCode, Resources resources, Activity activity, boolean isSettingsFragment) {
        // Criar um objeto Locale com o código do idioma
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        // Configurar a localidade no Resources
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        // Atualizar a configuração
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Salvar o idioma selecionado nas preferências (opcional)
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language_preference", languageCode);
        editor.apply();

        // Reiniciar a Activity para aplicar as mudanças
        if (isSettingsFragment)
            restartActivity(activity);
    }

    private static void restartActivity(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
    }
}