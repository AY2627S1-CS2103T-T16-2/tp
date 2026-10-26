package seedu.tuitionbook.model.person;

import java.util.List;
import java.util.function.Predicate;

import seedu.tuitionbook.commons.util.ToStringBuilder;

/**
 * Tests whether a {@code Person}'s name contains any given keyword using literal,
 * case-insensitive Unicode matching.
 */
public class NameContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final List<String> comparisonKeywords;

    /**
     * Creates a predicate that matches any of the given literal keywords.
     *
     * @param keywords Keywords to match case-insensitively against a person's name.
     */
    public NameContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = List.copyOf(keywords);
        comparisonKeywords = this.keywords.stream()
                .map(NameComparisonUtil::toUnicodeComparisonForm)
                .toList();
    }

    /** Returns the immutable keywords used to establish this query. */
    public List<String> getKeywords() {
        return keywords;
    }

    @Override
    public boolean test(Person person) {
        String comparisonName = NameComparisonUtil.toUnicodeComparisonForm(person.getName().fullName);
        return comparisonKeywords.stream().anyMatch(comparisonName::contains);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsKeywordsPredicate otherNameContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
