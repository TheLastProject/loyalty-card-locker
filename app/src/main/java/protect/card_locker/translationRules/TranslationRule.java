package protect.card_locker.translationRules;

public interface TranslationRule {
    String apply(String value);
    String undo(String value);
}

