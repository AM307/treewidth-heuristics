package at.mategka.sda.elimination.result;

public record VertexPrependResult<V>(V vertex, int degree) implements VertexResult<V> {
}
