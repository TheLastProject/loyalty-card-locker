package protect.card_locker.translationRules;

public class AddPrefixTranslationRule implements TranslationRule {
    String prefix;

    public AddPrefixTranslationRule(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String apply(String value) {
        return this.prefix + value;
    }

    @Override
    public String undo(String value) {
        if (!value.startsWith(this.prefix)) {
            throw new IllegalArgumentException();
        }

        return value.substring(this.prefix.length());
    }
}
