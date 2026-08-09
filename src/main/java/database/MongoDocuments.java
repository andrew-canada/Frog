package database;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Tolerant BSON helpers for reading both the original and current FlushID schemas.
 */
public final class MongoDocuments {
    private MongoDocuments() {
    }

    public static String id(final Document document) {
        final Object value = document.get("_id");
        return value instanceof final ObjectId objectId ? objectId.toHexString() : String.valueOf(value);
    }

    public static Document findById(final MongoCollection<Document> collection, final String id) {
        if (id == null || id.isBlank()) return null;
        if (ObjectId.isValid(id)) {
            return collection
                .find(Filters.or(Filters.eq("_id", new ObjectId(id)), Filters.eq("_id", id)))
                .first();
        }
        return collection
            .find(Filters.eq("_id", id))
            .first();
    }

    public static boolean referenceMatches(final Object stored, final String expected) {
        if (stored == null || expected == null) return false;
        final String actual = stored instanceof final ObjectId objectId ? objectId.toHexString() : stored.toString();
        return actual.equals(expected) || actual.contains(expected) || expected.contains(actual);
    }

    public static String string(final Document document, final String fallback, final String... keys) {
        for (final String key : keys) {
            final Object value = document.get(key);
            if (value != null && !value
                    .toString()
                    .isBlank()) {
                return value.toString();
            }
        }
        return fallback;
    }

    public static double number(final Document document, final double fallback, final String... keys) {
        for (final String key : keys) {
            final Object value = document.get(key);
            if (value instanceof final Number number) return number.doubleValue();
            if (value != null) {
                try {
                    return Double.parseDouble(value.toString());
                } catch (final NumberFormatException ignored) {
                }
            }
        }
        return fallback;
    }

    public static int integer(final Document document, final int fallback, final String... keys) {
        return (int) Math.round(number(document, fallback, keys));
    }

    public static boolean bool(final Document document, final boolean fallback, final String... keys) {
        for (final String key : keys) {
            final Object value = document.get(key);
            if (value instanceof final Boolean bool) return bool;
            if (value != null) return Boolean.parseBoolean(value.toString());
        }
        return fallback;
    }

    public static LocalDateTime dateTime(final Document document, final LocalDateTime fallback, final String... keys) {
        for (final String key : keys) {
            final Object value = document.get(key);
            if (value instanceof final Date date) return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            if (value instanceof final LocalDateTime dateTime) return dateTime;
            if (value instanceof final String text) {
                try {
                    return LocalDateTime.parse(text);
                } catch (final RuntimeException ignored) {
                }
            }
        }
        final Object id = document.get("_id");
        if (id instanceof final ObjectId objectId) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(objectId.getTimestamp()), ZoneId.systemDefault());
        }
        return fallback;
    }

    public static LocalDate date(final Document document, final LocalDate fallback, final String... keys) {
        return dateTime(document, fallback.atStartOfDay(), keys).toLocalDate();
    }
}
