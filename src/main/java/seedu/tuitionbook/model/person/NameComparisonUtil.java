package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Provides the shared Unicode transformation used when comparing names.
 */
final class NameComparisonUtil {

    private NameComparisonUtil() {
    }

    /**
     * Returns a locale-independent comparison form of {@code value}.
     *
     * <p>The lowercase step is needed before uppercasing so that capital sharp S and small sharp S both expand to
     * {@code SS}. This is a deliberately limited standard-library transformation, not complete Unicode case folding.
     */
    static String toUnicodeComparisonForm(String value) {
        requireNonNull(value);
        String normalizedValue = Normalizer.normalize(value, Normalizer.Form.NFC);
        String lowercasedValue = normalizedValue.toLowerCase(Locale.ROOT);
        String uppercasedValue = lowercasedValue.toUpperCase(Locale.ROOT);
        return Normalizer.normalize(uppercasedValue, Normalizer.Form.NFC);
    }
}
