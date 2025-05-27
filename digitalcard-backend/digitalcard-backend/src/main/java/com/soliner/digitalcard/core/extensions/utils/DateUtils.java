package com.soliner.digitalcard.core.extensions.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Uygulama genelinde tarih ve saat işlemleri için yardımcı sınıf.
 * Bu sınıf, herhangi bir katmanda kullanılabilir ve HTTP'ye bağımlı değildir.
 */
public class DateUtils {

    // Tarih formatları için sabitler
    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";

    /**
     * Verilen Date nesnesini belirtilen formata göre string'e dönüştürür.
     * @param date Formatlanacak Date nesnesi.
     * @param format Tarih formatı (örn: "yyyy-MM-dd").
     * @return Formatlanmış tarih string'i.
     */
    public static String format(Date date, String format) {
        if (date == null || format == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * Mevcut anı tam tarih ve saat formatında string olarak döndürür.
     * @return "yyyy-MM-dd HH:mm:ss" formatında mevcut zaman.
     */
    public static String getCurrentFormattedDateTime() {
        return format(new Date(), DATETIME_FORMAT_FULL);
    }
}
