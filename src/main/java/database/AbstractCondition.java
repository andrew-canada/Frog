package database;

import org.bson.conversions.Bson;

/**
 * Abstract condition class for allowing methods to accept either a CollectionCondition or a Condition.
 *
 * @param <T> the condition value type.
 */
public abstract class AbstractCondition<T> {

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public abstract Bson getFilter();

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public abstract String getFieldName();

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public abstract Operator getOperator();

    /**
     * Performs this operation.
     *
     * @return the operation result.
     */
    public abstract T getValue();
}
