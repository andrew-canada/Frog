package data_access;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Tolerant BSON helpers for reading both the original and current FlushID schemas. */
public final class MongoDocuments {
    private MongoDocuments() { }

    public static String id(Document document) {
        Object value = document.get("_id");
        return value instanceof ObjectId objectId ? objectId.toHexString() : String.valueOf(value);
    }

    public static Document findById(MongoCollection<Document> collection, String id) {
        if (id == null || id.isBlank()) return null;
        if (ObjectId.isValid(id)) {
            return collection.find(Filters.or(Filters.eq("_id", new ObjectId(id)), Filters.eq("_id", id))).first();
        }
        return collection.find(Filters.eq("_id", id)).first();
    }

    public static boolean referenceMatches(Object stored, String expected) {
        if (stored == null || expected == null) return false;
        String actual = stored instanceof ObjectId objectId ? objectId.toHexString() : stored.toString();
        return actual.equals(expected) || actual.contains(expected) || expected.contains(actual);
    }

    public static String string(Document document, String fallback, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value != null && !value.toString().isBlank()) return value.toString();
        }
        return fallback;
    }

    public static double number(Document document, double fallback, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value instanceof Number number) return number.doubleValue();
            if (value != null) try { return Double.parseDouble(value.toString()); } catch (NumberFormatException ignored) { }
        }
        return fallback;
    }

    public static int integer(Document document, int fallback, String... keys) {
        return (int) Math.round(number(document, fallback, keys));
    }

    public static boolean bool(Document document, boolean fallback, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value instanceof Boolean bool) return bool;
            if (value != null) return Boolean.parseBoolean(value.toString());
        }
        return fallback;
    }

    public static LocalDateTime dateTime(Document document, LocalDateTime fallback, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value instanceof Date date) return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            if (value instanceof LocalDateTime dateTime) return dateTime;
            if (value instanceof String text) try { return LocalDateTime.parse(text); } catch (RuntimeException ignored) { }
        }
        Object id = document.get("_id");
        if (id instanceof ObjectId objectId) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(objectId.getTimestamp()), ZoneId.systemDefault());
        }
        return fallback;
    }

    public static LocalDate date(Document document, LocalDate fallback, String... keys) {
        return dateTime(document, fallback.atStartOfDay(), keys).toLocalDate();
    }
}
