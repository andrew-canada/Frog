package data_access;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class representing a condition to be fed into the DatabaseAccessObject methods
 * such as Condition("buildingCode", Operator.EQ, "BA") for the condition that the buildingCode
 * field must equal "BA"
 * @param <T> the type of the value
 */
public class Condition<T extends Comparable<? super T>> {

    final Bson filter;

    public Condition(String fieldName, Operator operator, T value) {
        if (operator == Operator.EQ) {
            filter = Filters.eq(fieldName, value);
        } else if (operator == Operator.NE) {
            filter = Filters.ne(fieldName, value);
        } else if (operator == Operator.LT) {
            filter = Filters.lt(fieldName, value);
        } else if (operator == Operator.GT) {
            filter = Filters.gt(fieldName, value);
        } else if (operator == Operator.LTE) {
            filter = Filters.lte(fieldName, value);
        } else if (operator == Operator.GTE) {
            filter = Filters.gte(fieldName, value);
        } else if (operator == Operator.IN) {
            filter = Filters.in(fieldName, value);
        } else if (operator == Operator.NIN) {
            filter = Filters.nin(fieldName, value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Bson getFilter() {
        return filter;
    }

}
