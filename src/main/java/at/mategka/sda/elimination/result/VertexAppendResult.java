package at.mategka.sda.elimination.result;

public record VertexAppendResult<V>(V vertex, int degree) implements VertexResult<V> {}
