package data_access;

import org.bson.conversions.Bson;

abstract public class AbstractCondition<T> {

    abstract public Bson getFilter();

    abstract public String getFieldName();

    abstract public Operator getOperator();

    abstract public T getValue();
}
