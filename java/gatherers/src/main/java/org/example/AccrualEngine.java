package org.example;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

/// Create a gatherer that
/// * when created, it consumes the "current" date. The date up to which accruals
///   and carry-over expiries should be computed
///   * beginning of the year is assumed to be Jan 1st
///   * only year-end expiry and usages can be present after this date
/// * takes a stream of time-off changes of an employee of a single time-off type
///   in a year as an input
/// * adds to the stream time off-changes to handle
///   * carry-over expiry - fixed to 5 month
///   * accruals - proactively monthly 16 hours, with 48 hours limit
///   * year-end expiry - so that sum of all the time-off changes is 0
///
/// It is guaranties that time-off changes in the stream are ordered by their dates.
public class AccrualEngine {
    public static void main(String[] args) {
        List<TimeOffChange> changes = Stream.of(
                        new TimeOffChange(
                                LocalDate.of(2025, 1, 1),
                                ChangeType.CARRYOVER,
                                8),
                        new TimeOffChange(
                                LocalDate.of(2025, 1, 1),
                                ChangeType.ACCRUAL,
                                16)
                )
                .gather(accrualEngine(LocalDate.of(2025, 5, 13)))
                .toList();
        changes.forEach(System.out::println);
    }

    private static Gatherer<TimeOffChange, ?, TimeOffChange> accrualEngine(LocalDate dateGenerateChangesTo) {
        return carryoverExpiry(dateGenerateChangesTo)
                .andThen(accrue(dateGenerateChangesTo))
                .andThen(yearEndExpiry());
    }

    private static Gatherer<TimeOffChange, ?, TimeOffChange> carryoverExpiry(LocalDate dateGenerateChangesTo) {
        class State {
            int carryoverBalance = 0;
            boolean carryoverExpiryCreated = false;
        }
        final int carryoverLimitInMonths = 5;
        MonthDay monthDayOfCarryoverExpiry = MonthDay.of(1 + carryoverLimitInMonths, 1);
        boolean createExpiry = MonthDay.from(dateGenerateChangesTo).isAfter(monthDayOfCarryoverExpiry);
        return Gatherer.ofSequential(
                State::new,
                ((state, timeOffChange, downstream) -> {
                    boolean createCarryoverExpiryNow = createExpiry
                            && !state.carryoverExpiryCreated
                            && MonthDay.from(timeOffChange.date).isAfter(monthDayOfCarryoverExpiry)
                            && state.carryoverBalance > 0;
                    if (createCarryoverExpiryNow) {
                        state.carryoverExpiryCreated = true;
                        downstream.push(new TimeOffChange(
                                monthDayOfCarryoverExpiry.atYear(timeOffChange.date.getYear()),
                                ChangeType.CARRYOVER_EXPIRY,
                                -state.carryoverBalance));
                    }
                    return switch (timeOffChange.type) {
                        case CARRYOVER, USAGE -> {
                            if (createExpiry && !state.carryoverExpiryCreated) {
                                state.carryoverBalance += timeOffChange.hours;
                            }
                            yield downstream.push(timeOffChange);
                        }
                        case ACCRUAL -> downstream.push(timeOffChange);
                        case CARRYOVER_EXPIRY, YEAREND_EXPIRY -> !downstream.isRejecting();
                    };
                })
        );
    }
    private static Gatherer<TimeOffChange, ?, TimeOffChange> accrue(LocalDate dateGenerateChangesTo) {
        class State {
            Month month = Month.JANUARY;
            boolean accrualMetOrCreated = false; // TODO remove
        }
        final Month lastMonthToAccrue = dateGenerateChangesTo.getMonth();
        return Gatherer.ofSequential(
                State::new,
                (state, timeOffChange, downstream) -> {
                    if (timeOffChange.type == ChangeType.ACCRUAL
                            && timeOffChange.date.getMonth() == state.month
                            && timeOffChange.date.getDayOfMonth() == 1
                            && timeOffChange.date.getMonth().compareTo(lastMonthToAccrue) <= 0) {
                        state.month = state.month.plus(1);
                        return downstream.push(timeOffChange);
                    }
                    if (timeOffChange.type == ChangeType.ACCRUAL
                            && isBefore(timeOffChange.date.getMonth(), state.month)) {
                        return !downstream.isRejecting();
                    }
                    if (timeOffChange.type == ChangeType.ACCRUAL
                            && timeOffChange.date.getMonth().compareTo(state.month) > 0)) {
                        return !downstream.isRejecting();
                    }
                    if (timeOffChange.type == ChangeType.ACCRUAL
                            && timeOffChange.date.getMonth().compareTo(lastMonthToAccrue) > 0)) {
                        return !downstream.isRejecting();
                    }
                    if (timeOffChange.date.getMonth() > state.month &&) {}
                    if (timeOffChange.date.getMonth().isAf) {
                        return switch (timeOffChange.type) {
                            case CARRYOVER, USAGE, CARRYOVER_EXPIRY -> downstream.push(timeOffChange);
                            case ACCRUAL, YEAREND_EXPIRY -> !downstream.isRejecting();
                        };
                    }
                },
                (state, sink) -> {}
        );
    }
    private static Gatherer<TimeOffChange, ?, TimeOffChange> yearEndExpiry() {
        class State {
            int sum = 0;
            int year;
        }
        return Gatherer.of(
                State::new,
                Gatherer.Integrator.ofGreedy((state, timeOffChange, sink) -> {
                    state.year = timeOffChange.date.getYear();
                    if (timeOffChange.type != ChangeType.YEAREND_EXPIRY && !sink.isRejecting()) {
                        return sink.push(timeOffChange);
                    }
                    return !sink.isRejecting();
                }),
                (state1, state2) -> {
                    state1.sum += state2.sum;
                    return state1;
                },
                (state, sink) -> {
                    sink.push(new TimeOffChange(
                            LocalDate.of(state.year, 12, 31),
                            ChangeType.YEAREND_EXPIRY,
                            -state.sum));
                }
        );
    }

    private static boolean isBefore(Month a, Month b) {
        return a.compareTo(b) < 0;
    }

    private record TimeOffChange(
            LocalDate date,
            ChangeType type,
            int hours
    ) {
        TimeOffChange {
            if (!type.hoursValidator.test(hours)) {
                throw new IllegalArgumentException(type + " " + hours);
            }
        }
    }

    private enum ChangeType {
        ACCRUAL(ChangeType::isPositive),
        CARRYOVER(ChangeType::isPositive),
        USAGE(ChangeType::isNegative),
        CARRYOVER_EXPIRY(ChangeType::isNegative),
        YEAREND_EXPIRY(_ -> true);

        private final Predicate<Integer> hoursValidator;

        ChangeType(Predicate<Integer> hoursValidator) {
            this.hoursValidator = hoursValidator;
        }

        private static boolean isPositive(int hours) {
            return hours > 0;
        }

        private static boolean isNegative(int hours) {
            return hours < 0;
        }
    }
}
