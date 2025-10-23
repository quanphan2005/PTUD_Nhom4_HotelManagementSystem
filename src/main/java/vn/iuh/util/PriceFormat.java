package vn.iuh.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class PriceFormat {
    private static DecimalFormat priceFormatter = new DecimalFormat("#,###");

    public static DecimalFormat getPriceFormatter() {
        return priceFormatter;
    }

    public static String formatPrice(double price) {
        return priceFormatter.format(price);
    }

    public static BigDecimal lamTronDenHangNghin(BigDecimal price){
        BigDecimal thousand = new BigDecimal("1000");
        return price.divide(thousand, 0, RoundingMode.HALF_UP)
                .multiply(thousand);
    }
}
