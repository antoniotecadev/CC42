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
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.antonioteca.cc42.R;

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

    public static void startReaderNFC(NfcAdapter nfcAdapter, Activity activity, Context context, PendingIntent pendingIntent, IntentFilter[] intentFiltersArray, String[][] techListsArray) {
        if (nfcAdapter != null) {
            startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray);
            // 1. Criar o layout programaticamente
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 50, 50, 50);
            layout.setGravity(Gravity.CENTER_HORIZONTAL);

            // 2. Criar a ImageView
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.nfc_icon);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            imageParams.gravity = Gravity.CENTER_HORIZONTAL;
            imageView.setLayoutParams(imageParams);

            // 3. Adicionar a ImageView ao layout
            layout.addView(imageView);

            new AlertDialog.Builder(context)
                    .setIcon(R.drawable.baseline_connect_without_contact_24)
                    .setTitle(R.string.reader_nfc)
                    .setMessage(R.string.aprox_pass)
                    .setView(layout)
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.cancel), (dialog, which) -> {
                        nfcAdapter.disableForegroundDispatch(activity);
                        dialog.dismiss();
                    })
                    .show();
        } else {
            Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.nfc_not_suport), context, null);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) { // Verifica a versão do SDK do Android
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
