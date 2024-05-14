package at.mategka.sda.elimination.result;

public interface VertexResult<V> extends EliminationResult<V> {

    V vertex();

    int degree();

}
