package vn.iuh.util;

import java.text.DecimalFormat;

public class PriceFormat {
    private static DecimalFormat priceFormatter = new DecimalFormat("#,###");

    public static DecimalFormat getPriceFormatter() {
        return priceFormatter;
    }

    public static String formatPrice(double price) {
        return priceFormatter.format(price);
    }
}
