package database;

import org.bson.conversions.Bson;

import com.mongodb.client.model.Filters;

/**
 * Class representing a condition to be fed into the DatabaseAccessObject methods
 * such as Condition("buildingCode", Operator.EQ, "BA") for the condition that the buildingCode
 * field must equal "BA"
 *
 * @param <T> the type of the value
 */
public class Condition<T extends Comparable<? super T>> extends AbstractCondition<T> {

    private final Bson filter;

    private final String fieldName;
    private final Operator operator;
    private final T value;

    public Condition(final String fieldName, final Operator operator, final T value) {
        if (operator == Operator.EQ) {
            filter = Filters.eq(fieldName, value);
        }
        else if (operator == Operator.NE) {
            filter = Filters.ne(fieldName, value);
        }
        else if (operator == Operator.LT) {
            filter = Filters.lt(fieldName, value);
        }
        else if (operator == Operator.GT) {
            filter = Filters.gt(fieldName, value);
        }
        else if (operator == Operator.LTE) {
            filter = Filters.lte(fieldName, value);
        }
        else if (operator == Operator.GTE) {
            filter = Filters.gte(fieldName, value);
        }
        else {
            throw new IllegalArgumentException();
        }

        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public Bson getFilter() {
        return filter;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public T getValue() {
        return value;
    }

}

