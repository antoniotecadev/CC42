package com.antonioteca.cc42.model;

import android.graphics.Bitmap;

public record MealQrCode(String id, String mealName, String mealDescription, int campusId, int cursusId, Bitmap bitmapQrCode) { }
