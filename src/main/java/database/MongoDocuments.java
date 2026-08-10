package database;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

/**
 * Tolerant BSON helpers for reading both the original and current FlushID schemas.
 */
public final class MongoDocuments {
    private static final String FIELD_ID = "_id";

    private MongoDocuments() {
    }

    /**
     * Performs this operation.
     *
     * @param document parameter value.
     *
     * @return the operation result.
     */
    public static String id(final Document document) {
        final Object value = document.get(FIELD_ID);
        final String result;
        if (value instanceof final ObjectId objectId) {
            result = objectId.toHexString();
        }
        else {
            result = String.valueOf(value);
        }
        return result;
    }

    /**
     * Performs this operation.
     *
     * @param collection parameter value.
     *
     * @param id parameter value.
     *
     * @return the operation result.
     */
    public static Document findById(final MongoCollection<Document> collection, final String id) {
        final Document result;
        if (id == null || id.isBlank()) {
            result = null;
        }
        else if (ObjectId.isValid(id)) {
            result = collection
                .find(Filters.or(Filters.eq(FIELD_ID, new ObjectId(id)), Filters.eq(FIELD_ID, id)))
                .first();
        }
        else {
            result = collection
                .find(Filters.eq(FIELD_ID, id))
                .first();
        }
        return result;
    }

    /**
     * Performs this operation.
     *
     * @param stored parameter value.
     *
     * @param expected parameter value.
     *
     * @return the operation result.
     */
    public static boolean referenceMatches(final Object stored, final String expected) {
        if (stored == null || expected == null) {
            return false;
        }
        final String actual;
        if (stored instanceof final ObjectId objectId) {
            actual = objectId.toHexString();
        }
        else {
            actual = stored.toString();
        }
        return actual.equals(expected) || actual.contains(expected) || expected.contains(actual);
    }

    /**
     * Performs this operation.
     *
     * @param document parameter value.
     *
     * @param fallback parameter value.
     *
     * @param keys parameter value.
     *
     * @return the operation result.
     */
    public static String string(final Document document, final String fallback, final String... keys) {
        String result = fallback;
        for (final String key : keys) {
            final Object value = document.get(key);
            if (value != null && !value
                .toString()
                .isBlank()) {
                result = value.toString();
                break;
            }
        }
        return result;
    }

    /**
     * Performs this operation.
     *
     * @param document parameter value.
     *
     * @param fallback parameter value.
     *
     * @param keys parameter value.
     *
     * @return the operation result.
     */
    public static double number(final Document document, final double fallback, final String... keys) {
        double result = fallback;
        for (final String key : keys) {
            final Object value = document.get(key);
            if (value instanceof final Number number) {
                result = number.doubleValue();
                break;
            }
            if (value != null) {
                try {
                    result = Double.parseDouble(value.toString());
                    break;
                }
                catch (final NumberFormatException ignored) {
                    // A later key may contain a valid numeric value.
                }
            }
        }
        return result;
    }

    /**
     * Performs this operation.
     *
     * @param document parameter value.
     *
     * @param fallback parameter value.
     *
     * @param keys parameter value.
     *
     * @return the operation result.
     */
    public static int integer(final Document document, final int fallback, final String... keys) {
        return (int) Math.round(number(document, fallback, keys));
    }

    /**
     * Performs this operation.
     *
     * @param document parameter value.
     *
     * @param fallback parameter value.
     *
     * @param keys parameter value.
     *
     * @return the operation result.
     */
    public static boolean bool(final Document document, final boolean fallback, final String... keys) {
        boolean result = fallback;
        for (final String key : keys) {
            final Object value = document.get(key);
            if (value instanceof final Boolean bool) {
                result = bool;
                break;
            }
            if (value != null) {
                result = Boolean.parseBoolean(value.toString());
                break;
            }
        }
        return result;
    }

    /**
     * Performs this operation.
     *
     * @param document parameter value.
     *
     * @param fallback parameter value.
     *
     * @param keys parameter value.
     *
     * @return the operation result.
     */
    public static LocalDateTime dateTime(final Document document, final LocalDateTime fallback, final String... keys) {
        LocalDateTime result = fallback;
        boolean found = false;
        for (final String key : keys) {
            final Object value = document.get(key);
            if (value instanceof final Date date) {
                result = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                found = true;
                break;
            }
            if (value instanceof final LocalDateTime dateTime) {
                result = dateTime;
                found = true;
                break;
            }
            if (value instanceof final String text) {
                try {
                    result = LocalDateTime.parse(text);
                    found = true;
                    break;
                }
                catch (final RuntimeException ignored) {
                    // Try the next schema key.
                }
            }
        }
        if (!found) {
            final Object id = document.get(FIELD_ID);
            if (id instanceof final ObjectId objectId) {
                result = LocalDateTime.ofInstant(Instant.ofEpochSecond(objectId.getTimestamp()), ZoneId.systemDefault());
            }
        }
        return result;
    }

    /**
     * Performs this operation.
     *
     * @param document parameter value.
     *
     * @param fallback parameter value.
     *
     * @param keys parameter value.
     *
     * @return the operation result.
     */
    public static LocalDate date(final Document document, final LocalDate fallback, final String... keys) {
        return dateTime(document, fallback.atStartOfDay(), keys).toLocalDate();
    }
}
