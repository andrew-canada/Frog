# FlushID

FlushID is a Java Swing application for finding U of T washrooms, reading reviews, receiving a recommendation, submitting live status reports, viewing busyness data, and requesting walking directions.

## Before running

Requirements:

- JDK 21 or newer
- Maven 3.9 or newer, or IntelliJ IDEA with Maven support
- A MongoDB connection string with access to the `FlushID` database
- A GraphHopper API key
- A Gemini API key only if you use **Personal Washroom Plan**

Set credentials in the process environment. Do not paste them into Java source, the POM, or a committed configuration file.

```powershell
$env:MONGODB_URI = Read-Host "MongoDB connection string"
$env:MONGODB_DATABASE = "FlushID"
$env:GRAPHHOPPER_API_KEY = Read-Host "GraphHopper API key"
$env:GEMINI_API_KEY = Read-Host "Gemini API key"
```

The database-name variable is optional and defaults to `FlushID`.

## Run and test

From the repository root:

```powershell
mvn clean test
mvn compile exec:java
```

You can also run `app.Main` directly from IntelliJ. Add `MONGODB_URI`, `MONGODB_DATABASE`, `GRAPHHOPPER_API_KEY`, and (for Personal Washroom Plan) `GEMINI_API_KEY` to the Main run configuration's environment variables first.

## What is live

- `Buildings` and `Washrooms` are read from MongoDB when the app starts.
- `Reviews` are read from MongoDB; the original `stars`/`text` schema and the newer `rating`/`comment` schema are both accepted.
- Signup writes salted PBKDF2 password hashes to `Users`; plaintext passwords are never written.
- Login reads the stored password hash and keeps only the current session user in memory.
- Status submission writes to `StatusReports`, and recommendation/busyness use those stored reports.
- Busyness can also read timetable-derived rows from `EnrollmentMeetings`. If that collection has no data, the UI says that data is unavailable.
- Directions call `https://graphhopper.com/api/1/route` using the `foot` profile. The returned route geometry, distance, and duration are rendered over the street map. The same configured GraphHopper key also resolves an entered street address through `/geocode`; coordinates remain available as an alternative.
- Startup idempotently upserts selectable Bahen Centre, Myhal Centre, Trinity College, and Hart House records into both `Buildings` and `Washrooms`.
- The map uses JXMapViewer2 with live OpenStreetMap street tiles, persistent local tile caching, visible attribution, drag-to-pan, wheel zoom, campus markers, and the GraphHopper route overlay.

At the time of integration, the live database contained `Buildings` and `Washrooms`; reviews, users, status reports, and enrollment rows will remain empty until they are created or imported.

## Architecture

- `entity`: domain entities and value objects.
- `use_case`: application rules, input/output boundaries, and gateway interfaces.
- `interface_adapter`: controllers, presenters, and observable view models.
- `data_access`: MongoDB and GraphHopper implementations of the gateway contracts.
- `view`: Swing layouts and route rendering.
- `app.AppBuilder`: the composition root and only place concrete adapters are selected.

Unit tests use fake gateway interfaces, so application rules are tested without MongoDB or network access. The GraphHopper adapter test uses a local HTTP server to verify request parameters and response parsing without consuming API credits.
