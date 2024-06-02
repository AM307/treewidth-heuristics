package at.mategka.sda.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public record Vector2<T>(T first, T second) implements Iterable<T> {

    public static <T> Function<Vector2<T>, String> formatted(String format) {
        return (v) -> String.format(format, v.first, v.second);
    }

    public static <T, U> Function<Vector2<T>, Vector2<U>> mapping(Function<T, U> mapper) {
        return (v) -> v.map(mapper);
    }

    public Vector2<T> reversed() {
        return new Vector2<>(second, first);
    }

    public Vector2<T> sorted() {
        return Objects.equals(stream().sorted().findFirst().orElseThrow(), first) ? this : reversed();
    }

    public Vector2<T> sorted(Comparator<T> comparator) {
        return Objects.equals(min(comparator), first) ? this : reversed();
    }

    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return accumulator.apply(accumulator.apply(identity, first), second);
    }

    public T reduce(BinaryOperator<T> accumulator) {
        return accumulator.apply(first, second);
    }

    public T min(Comparator<T> comparator) {
        return comparator.compare(first, second) <= 0 ? first : second;
    }

    public T max(Comparator<T> comparator) {
        return comparator.compare(first, second) >= 0 ? first : second;
    }

    public boolean anyMatch(Predicate<T> predicate) {
        return predicate.test(first) || predicate.test(second);
    }

    public boolean allMatch(Predicate<T> predicate) {
        return predicate.test(first) && predicate.test(second);
    }

    public boolean noneMatch(Predicate<T> predicate) {
        return !anyMatch(predicate);
    }

    public List<T> toList() {
        return List.of(first, second);
    }

    public Set<T> toSet() {
        return Set.of(first, second);
    }

    public <U> Vector2<U> map(Function<T, U> mapper) {
        return new Vector2<>(mapper.apply(first), mapper.apply(second));
    }

    @Override
    public Iterator<T> iterator() {
        return toList().iterator();
    }

    public Stream<T> stream() {
        return Stream.of(first, second);
    }
}
