package com.rassafel.io.storage.core.impl.keygen;

import com.rassafel.io.storage.core.impl.KeyGenerator;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.lang.Nullable;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;

import static java.time.temporal.ChronoField.*;


/**
 * The calendar based key generator. Ignores source name.
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class CalendarKeyGenerator implements KeyGenerator {
    /**
     * Date formatter that formats a date, such as '2024/04/29'.
     */
    protected static final DateTimeFormatter DEFAULT_FORMATTER = new DateTimeFormatterBuilder()
        .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .appendLiteral(SEPARATOR)
        .appendValue(MONTH_OF_YEAR, 2)
        .appendLiteral(SEPARATOR)
        .appendValue(DAY_OF_MONTH, 2)
        .toFormatter();

    private Clock clock = Clock.systemUTC();
    private DateTimeFormatter formatter = DEFAULT_FORMATTER;

    @Override
    public String createKey(@Nullable String name) {
        val date = LocalDate.now(clock);
        return formatter.format(date);
    }
}
