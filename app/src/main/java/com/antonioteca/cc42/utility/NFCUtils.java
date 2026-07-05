package com.antonioteca.cc42.utility;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.LayoutQuantityBinding;
import com.antonioteca.cc42.databinding.RadioGroupCheckBinding;
import com.antonioteca.cc42.databinding.RadioGroupPortionBinding;

import java.io.UnsupportedEncodingException;

public class NFCUtils {
    public static Object[] startNFC(@NonNull NfcAdapter nfcAdapter, Activity activity) {

        // Cria o PendingIntent que será disparado pelo sistema NFC
        // Chamar onNewIntent da Activity hospedeira.
        //  Quando o cartão NFC for detectado, essa Intent será disparada para a Activity.
        Intent intent = new Intent(activity, activity.getClass()); // Cria uma nova Intent para a actividade actual
        // SINGLE_TOP: Se a Activity já estiver no topo, ela não será recriada, só reutilizada.
        // CLEAR_TOP: Se houver outras Activities por cima, elas são removidas.
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); // Flag para iniciar a actividade em uma nova instância

        final int flags = NFCUtils.getFlags();
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, flags); // Cria o PendingIntent

        //  Diz ao sistema que você só quer tratar cartões que forem reconhecidos por tecnologia (TECH_DISCOVERED).
        //  Evita Que o app reaja a qualquer NFC aleatório (como tags de outro tipo).
        IntentFilter techFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED); // Filtro para tecnologias NFC
        IntentFilter[] intentFiltersArray = new IntentFilter[]{techFilter}; // Array de filtros de intents
        // Diz ao sistema que seu app consegue ler cartões do tipo NfcA e MifareClassic
        // Define um array de Strings para as tecnologias NFC
        // Usar o nome da classe da tecnologia
        // Usar o nome da classe da tecnologia
        String[][] techListsArray = new String[][]{ // Diz ao sistema que seu app consegue ler cartões do tipo NfcA e MifareClassic
                new String[]{ // Define um array de Strings para as tecnologias NFC
                        NfcA.class.getName(), // Usar o nome da classe da tecnologia
                        MifareClassic.class.getName() // Usar o nome da classe da tecnologia
                }
        };
        return new Object[]{nfcAdapter, pendingIntent, intentFiltersArray, techListsArray};
    }

    public static void startReaderNFC(NfcAdapter nfcAdapter, Activity activity, Context context, PendingIntent pendingIntent, IntentFilter[] intentFiltersArray, String[][] techListsArray, RadioGroupCheckBinding radioGroupCheck, RadioGroupPortionBinding radioGroupPortion, LayoutQuantityBinding layoutQuantity) {
        if (nfcAdapter != null) {
            startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray);
            // 1. Criar o layout programaticamente
            LinearLayout dialogLayout = new LinearLayout(context);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(50, 0, 50, 50);
            dialogLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            // 2. Criar a ImageView
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.nfc_icon);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            imageParams.gravity = Gravity.CENTER_HORIZONTAL;
            imageView.setLayoutParams(imageParams);
            ViewGroup parent;
            if (radioGroupCheck != null) {
                // Obtém o pai da view que queremos mover
                parent = (ViewGroup) radioGroupCheck.getRoot().getParent();
                if (parent != null) {
                    // Remove a view do seu pai actual
                    parent.removeView(radioGroupCheck.getRoot());
                }
                radioGroupCheck.radioGroupEventCheck.setVisibility(View.VISIBLE);
                radioGroupPortion.checkBoxBlocked.setVisibility(View.VISIBLE);
                // Itera sobre todos os RadioButtons dentro do RadioGroup
                setColorText(context, radioGroupCheck, null, R.color.textColorPrimary);
                dialogLayout.addView(radioGroupCheck.getRoot());
            } else {
                parent = (ViewGroup) radioGroupPortion.getRoot().getParent();
                if (parent != null) {
                    // Remove a view do seu pai actual
                    parent.removeView(radioGroupPortion.getRoot());
                }
                radioGroupPortion.radioGroupMealPortion.setVisibility(View.VISIBLE);
                // Itera sobre todos os RadioButtons dentro do RadioGroup
                setColorText(context, null, radioGroupPortion, R.color.textColorPrimary);
                dialogLayout.addView(radioGroupPortion.getRoot());
            }
            dialogLayout.addView(imageView);
            if (layoutQuantity != null) {
                LayoutQuantityBinding dialogQuantityBinding = LayoutQuantityBinding.inflate(activity.getLayoutInflater());
                dialogQuantityBinding.liniearLayoutQuantity.setVisibility(View.VISIBLE);

                LinearLayout.LayoutParams liniearLayoutQuantityValue = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                liniearLayoutQuantityValue.setMargins(0, 0, 0, 0);
                dialogQuantityBinding.liniearLayoutQuantityValue.setLayoutParams(liniearLayoutQuantityValue);

                dialogQuantityBinding.textViewQuantity.setTextColor(context.getResources().getColor(R.color.textColorPrimary, context.getTheme()));
                dialogQuantityBinding.textViewQuantityValue.setTextColor(context.getResources().getColor(R.color.textColorPrimary, context.getTheme()));
                dialogQuantityBinding.buttonDecrement.setTextColor(context.getResources().getColor(R.color.textColorPrimary, context.getTheme()));
                dialogQuantityBinding.buttonIncrement.setTextColor(context.getResources().getColor(R.color.textColorPrimary, context.getTheme()));
                dialogQuantityBinding.buttonDecrement.setOnClickListener(v -> {
                    int currentQuantity = Integer.parseInt(dialogQuantityBinding.textViewQuantityValue.getText().toString());
                    if (currentQuantity > 1) {
                        dialogQuantityBinding.textViewQuantityValue.setText(String.valueOf(currentQuantity - 1));
                        layoutQuantity.textViewQuantityValue.setText(String.valueOf(currentQuantity - 1));
                    }
                });

                dialogQuantityBinding.buttonIncrement.setOnClickListener(v -> {
                    int currentQuantity = Integer.parseInt(dialogQuantityBinding.textViewQuantityValue.getText().toString());
                    if (currentQuantity < 9) {
                        dialogQuantityBinding.textViewQuantityValue.setText(String.valueOf(currentQuantity + 1));
                        layoutQuantity.textViewQuantityValue.setText(String.valueOf(currentQuantity + 1));
                    }
                });
                dialogLayout.addView(dialogQuantityBinding.getRoot());
            }

            new AlertDialog.Builder(context)
                    .setIcon(R.drawable.baseline_connect_without_contact_24)
                    .setTitle(R.string.reader_nfc)
                    .setMessage(R.string.aprox_pass)
                    .setView(dialogLayout)
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.cancel), (dialog, which) -> {
                        // ATENÇÃO: É preciso adicionar a view de volta ao seu pai original
                        // se o diálogo for cancelado, para evitar problemas na UI.
                        if (parent != null) {
                            if (radioGroupCheck != null) {
                                // 1. Remove a view do seu pai ACTUAL (o layout do diálogo)
                                radioGroupCheck.radioGroupEventCheck.setVisibility(View.GONE);
                                radioGroupPortion.checkBoxBlocked.setVisibility(View.GONE);

                                setColorText(context, radioGroupCheck, null, R.color.white);
                                dialogLayout.removeView(radioGroupCheck.getRoot());
                                // 2. Agora sim, adiciona a view de volta ao seu pai ORIGINAL
                                parent.addView(radioGroupCheck.getRoot());
                            } else {
                                radioGroupPortion.radioGroupMealPortion.setVisibility(View.GONE);
                                setColorText(context, null, radioGroupPortion, R.color.white);
                                dialogLayout.removeView(radioGroupPortion.getRoot());
                                parent.addView(radioGroupPortion.getRoot());
                            }
                        }
                        nfcAdapter.disableForegroundDispatch(activity);
                        dialog.dismiss();
                    })
                    .show();
        } else {
            Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.nfc_not_suport), context, null);
        }
    }

    private static void setColorText(Context context, RadioGroupCheckBinding radioGroupCheck, RadioGroupPortionBinding radioGroupPortion, int color) {
        if (radioGroupCheck != null) {
            for (int i = 0; i < radioGroupCheck.radioGroupEventCheck.getChildCount(); i++) {
                View child = radioGroupCheck.radioGroupEventCheck.getChildAt(i);
                // Verifica se o filho é um RadioButton antes de fazer o cast
                if (child instanceof RadioButton) {
                    // Define a cor do texto para cada RadioButton
                    ((RadioButton) child).setTextColor(context.getResources().getColor(color, context.getTheme()));
                }
            }
        } else {
            for (int i = 0; i < radioGroupPortion.radioGroupMealPortion.getChildCount(); i++) {
                View child = radioGroupPortion.radioGroupMealPortion.getChildAt(i);
                // Verifica se o filho é um RadioButton antes de fazer o cast
                if (child instanceof RadioButton) {
                    // Define a cor do texto para cada RadioButton
                    ((RadioButton) child).setTextColor(context.getResources().getColor(color, context.getTheme()));
                }
            }
        }
    }


    public static void startReaderNFC(@NonNull NfcAdapter nfcAdapter, Activity activity, PendingIntent pendingIntent, IntentFilter[] intentFiltersArray, String[][] techListsArray) {
        nfcAdapter.enableForegroundDispatch(
                activity,
                pendingIntent,
                intentFiltersArray,
                techListsArray
        );
    }

    public static int getFlags() {
        int flags; // Variável para armazenar as flags do PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Verifica a versão do SDK do Android
            //  A partir do Android 12 (API 31, codinome “S”), o sistema exige que o PendingIntent seja mutável se você pretende alterar os dados da Intent.
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE; // Define as flags para versões S e superiores
        } else { // Para versões anteriores ao S
            flags = PendingIntent.FLAG_UPDATE_CURRENT; // Define a flag para actualizar o PendingIntent atual
        }
        return flags;
    }

    @NonNull
    public static String getString(@NonNull NdefRecord record) throws UnsupportedEncodingException {
        byte[] payload = record.getPayload();

        // O primeiro byte do payload contém informações de status e codificação
        // Determina a codificação do texto (UTF-8 ou UTF-16) com base no bit mais significativo do primeiro byte
        String encoding = (payload[0] & 0x80) == 0 ? "UTF-8" : "UTF-16";
        // Obtém o comprimento do código do idioma (os 6 bits menos significativos do primeiro byte)
        int languageCodeLength = payload[0] & 0x3F;

        // O texto real começa após o byte de status e o código do idioma
        // Cria uma string a partir do payload, especificando o offset inicial, o comprimento e a codificação
        return new String(payload, languageCodeLength + 1,
                payload.length - languageCodeLength - 1, encoding);
    }

    public static void showAlertDialogBuild(String title, String message, Context context, Runnable runnableTryAgain) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(R.drawable.logo_42);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> runnableTryAgain.run());
        builder.show();
    }
}
