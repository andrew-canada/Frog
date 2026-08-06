package data_access;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.Collection;

public class CollectionCondition<T extends Collection<?>> extends AbstractCondition<T> {

    private final Bson filter;

    private final String fieldName;
    private final Operator operator;
    private final T value;

    public CollectionCondition(String fieldName, Operator operator, T value) {
        if (operator == Operator.IN) {
            filter = Filters.in(fieldName, (Iterable<?>) value);
        } else if (operator == Operator.NIN) {
            filter = Filters.nin(fieldName, (Iterable<?>) value);
        } else {
            throw new IllegalArgumentException();
        }

        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
    }

    public Bson getFilter() {
        return filter;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Operator getOperator() {
        return operator;
    }

    public T getValue() {
        return value;
    }

}
