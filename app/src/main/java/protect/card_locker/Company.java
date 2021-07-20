package protect.card_locker;

import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.List;

import protect.card_locker.translationRules.AddPrefixTranslationRule;
import protect.card_locker.translationRules.TranslationRule;

public class Company {
    private final String name;
    private final List<BarcodeFormat> barcodeFormats;
    private final List<TranslationRule> translationRuleList;

    private Company(final Builder builder) {
        name = builder.name;
        barcodeFormats = builder.barcodeFormats;
        translationRuleList = builder.translationRuleList;
    }

    public String getName() {
        return name;
    }

    public String cardIDToBarcode(String cardID) {
        String barcode = cardID;
        for (int i = 0; i < translationRuleList.size(); i++) {
            barcode = translationRuleList.get(i).apply(barcode);
        }

        return barcode;
    }

    public String BarcodeToCardID(String barcode) {
        String cardID = barcode;
        for (int i = translationRuleList.size() - 1; i > 0; i--) {
            cardID = translationRuleList.get(i).undo(cardID);
        }

        return cardID;
    }

    static class Builder {
        private final String name;
        private final List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        private final List<TranslationRule> translationRuleList = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder addBarcodeFormat(final BarcodeFormat barcodeFormat) {
            barcodeFormats.add(barcodeFormat);
            return this;
        }

        public Builder addPrefix(final String prefix) {
            translationRuleList.add(new AddPrefixTranslationRule(prefix));
            return this;
        }

        public Company create() {
            return new Company(this);
        }
    }
}