package com.marketviz.model;

import java.time.LocalDate;

/**
 * Named time windows that a widget can display.
 *
 * <p>Each constant resolves itself to a concrete start date so that service
 * and controller layers never duplicate the date-arithmetic logic.
 */
public enum TimeRange {

    ONE_MONTH {
        @Override public LocalDate startDate() { return LocalDate.now().minusMonths(1); }
    },
    THREE_MONTHS {
        @Override public LocalDate startDate() { return LocalDate.now().minusMonths(3); }
    },
    SIX_MONTHS {
        @Override public LocalDate startDate() { return LocalDate.now().minusMonths(6); }
    },
    ONE_YEAR {
        @Override public LocalDate startDate() { return LocalDate.now().minusYears(1); }
    },
    THREE_YEARS {
        @Override public LocalDate startDate() { return LocalDate.now().minusYears(3); }
    },
    FIVE_YEARS {
        @Override public LocalDate startDate() { return LocalDate.now().minusYears(5); }
    };

    /** Returns the earliest date included in this time-range window. */
    public abstract LocalDate startDate();
}
