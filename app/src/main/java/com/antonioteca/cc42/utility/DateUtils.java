package com.antonioteca.cc42.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    // Função para fazer o parse da data
    public static Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        try {
            return dateFormat.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Função para extrair o dia, o mês e a hora
    public static String getFormattedDate(Date date, String pattern) {
        // day = Extrair o dia (23, 24, etc)
        // month = Extrair o mês em inglês (January, February, etc)
        // time = Extrair a hora no formato 12 horas com AM/PM
        if (date != null) {
            SimpleDateFormat dayFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
            return dayFormat.format(date);
        } else
            return null;
    }

    // Função para calcular os dias até a data
    public static String getDaysUntil(Date eventDate) {
        if (eventDate != null) {
            Date currentDate = new Date(); // Pega a data actual
            long diffInMillies = eventDate.getTime() - currentDate.getTime();
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            if (diffInDays > 1) {
                return diffInDays + " days";
            } else if (diffInDays == 1) {
                return "1 day";
            } else if (diffInDays == 0) {
                return "today";
            } else {
                return "finished"; // Caso a data já tenha passado
            }
        } else
            return null;
    }

    // Função para calcular oduração do evento em hora
    public static String getEventDuration(Date eventDateBegin, Date eventDateEnd) {
        if (eventDateBegin != null && eventDateEnd != null) {
            long diffInMillies = eventDateEnd.getTime() - eventDateBegin.getTime();
            long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
            return diffInMinutes + " minutes";
        } else
            return null;
    }
}
