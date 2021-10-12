package co.com.demo.rest.contracts;

import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface ElasticIndexes {

    Supplier<String> all();

    boolean existsIndex(String indexName, IntPredicate statusPredicate);

    UnaryOperator<String> create();

    UnaryOperator<String> getIndex();

    UnaryOperator<String> delete();

}
