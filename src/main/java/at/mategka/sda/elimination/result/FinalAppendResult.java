package at.mategka.sda.elimination.result;

import java.util.Set;

public record FinalAppendResult<V>(Set<V> vertices) implements EliminationResult<V> {}
