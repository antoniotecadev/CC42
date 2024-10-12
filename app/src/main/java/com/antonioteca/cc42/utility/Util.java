package com.antonioteca.cc42.utility;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;

import com.antonioteca.cc42.R;

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
}
