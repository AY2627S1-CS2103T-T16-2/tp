package seedu.tuitionbook.model.person;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import seedu.tuitionbook.commons.util.ToStringBuilder;

/**
 * Tests whether a {@code Person}'s name contains any given keyword using literal,
 * case-insensitive Unicode matching.
 */
public class NameContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final List<Pattern> keywordPatterns;

    /**
     * Creates a predicate that matches any of the given literal keywords.
     *
     * @param keywords Keywords to match case-insensitively against a person's name.
     */
    public NameContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = List.copyOf(keywords);
        keywordPatterns = this.keywords.stream()
                .map(keyword -> Pattern.compile(Pattern.quote(keyword),
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
                .toList();
    }

    @Override
    public boolean test(Person person) {
        String fullName = person.getName().fullName;
        return keywordPatterns.stream()
                .anyMatch(pattern -> pattern.matcher(fullName).find());
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
