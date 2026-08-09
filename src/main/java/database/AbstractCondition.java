package database;

import org.bson.conversions.Bson;

public abstract class AbstractCondition<T> {

    public abstract Bson getFilter();

    public abstract String getFieldName();

    public abstract Operator getOperator();

    public abstract T getValue();
}
