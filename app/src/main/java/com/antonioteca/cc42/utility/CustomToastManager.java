package com.antonioteca.cc42.utility;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.antonioteca.cc42.R;

public class CustomToastManager {

    private static LinearLayout container;

    private static void initContainer(Activity activity) {
        if (container == null || container.getParent() == null) {
            // Pega o view root da Activity
            ViewGroup rootView = activity.findViewById(android.R.id.content);

            container = new LinearLayout(activity);
            container.setOrientation(LinearLayout.VERTICAL);

            // Posiciona no Canto Superior Direito (Top | End)
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.TOP | Gravity.END;
            params.topMargin = 60; // Ajuste conforme necessário para não cobrir a AppBar
            params.rightMargin = 20;

            container.setLayoutParams(params);
            rootView.addView(container);
        }
    }

    public static void showNotification(
            Context context,
            LayoutInflater inflater,
            String title,
            String message,
            String urlImageUser,
            Runnable onNotificationClick // Ação para abrir o AlertDialog
    ) {
        if (!(context instanceof Activity activity)) return;

        // Garante que roda na UI Thread
        activity.runOnUiThread(() -> {
            initContainer(activity);

            // Infla o layout customizado
            View toastView = inflater.inflate(R.layout.custom_toast_layout, container, false);
            TextView tvTitle = toastView.findViewById(R.id.toastTitle);
            TextView tvMessage = toastView.findViewById(R.id.toastMessage);

            tvTitle.setText(title);
            tvMessage.setText(message);

            // Caso o usuário clique na notificação antes dela sumir
            toastView.setOnClickListener(v -> {
                container.removeView(toastView); // Remove o toast
                if (onNotificationClick != null) {
                    onNotificationClick.run(); // Abre o AlertDialog
                }
            });

            // Adiciona no topo da lista interna (um abaixo do outro)
            container.addView(toastView);

            // Remove automaticamente após 4 segundos
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (container != null) {
                    container.removeView(toastView);
                }
            }, 5000); // 5000ms = 5 segundos
        });
    }
}
