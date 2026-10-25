package seedu.tuitionbook.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NameComparisonUtilTest {

    @Test
    public void toUnicodeComparisonForm_requiredEquivalentForms_returnsSameValue() {
        assertEquals(NameComparisonUtil.toUnicodeComparisonForm("Groß"),
                NameComparisonUtil.toUnicodeComparisonForm("GROẞ"));
        assertEquals(NameComparisonUtil.toUnicodeComparisonForm("Straße"),
                NameComparisonUtil.toUnicodeComparisonForm("STRASSE"));
        assertEquals(NameComparisonUtil.toUnicodeComparisonForm("Jos\u00e9"),
                NameComparisonUtil.toUnicodeComparisonForm("Jose\u0301"));
    }

    @Test
    public void toUnicodeComparisonForm_sharpSForms_expandsToDoubleS() {
        assertEquals("GROSS", NameComparisonUtil.toUnicodeComparisonForm("Groß"));
        assertEquals("GROSS", NameComparisonUtil.toUnicodeComparisonForm("GROẞ"));
    }
}
