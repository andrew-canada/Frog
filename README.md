# FlushID

FlushID is a Java Swing desktop application for finding University of Toronto washrooms. It combines an interactive campus map with washroom details, reviews, live crowd reports, directions, and personal planning.

## Features

- Browse washrooms on a draggable, zoomable OpenStreetMap campus map.
- Search, filter, and sort washrooms by distance, rating, accessibility, gender, building, current status, your reviews, and personal-plan availability.
- View, write, sort, vote on, and report washroom reviews.
- Create an account, log in or out, and update account details.
- Submit live busyness, cleanliness, and maintenance reports; view heat-map and busyness information.
- Request walking directions from a map coordinate or a geocoded street address.
- Generate an optional personal washroom plan from an `.ics` timetable using Gemini.
- Moderate reported reviews with designated moderator accounts.

## Requirements

- JDK 24, matching `maven.compiler.release` in `pom.xml`.
- Maven 3.9+ to build from source.
- A graphical desktop environment and internet access for the map and routing service.
- MongoDB database access containing the project washroom/building data.
- A GraphHopper API key. Gemini is required only when generating a personal plan.

## Configuration

The built JAR bundles `src/main/resources/environment_variables.env` and reads it at startup. The file supplies the MongoDB, GraphHopper, and Gemini values needed by the submission/demo build.

| Setting | Required | Purpose |
| --- | --- | --- |
| `MONGODB_URI` | Yes | MongoDB connection string. |
| `MONGODB_DATABASE` | No | Database name; defaults to `FlushID`. |
| `GRAPHHOPPER_API_KEY` | Yes | Address lookup and walking directions. |
| `GEMINI_API_KEY` | Only for personal plans | Gemini timetable-plan generation. |

A non-blank process environment variable takes precedence over the bundled value. For example, to override the current PowerShell session:

```powershell
$env:MONGODB_URI = Read-Host "MongoDB connection string"
$env:MONGODB_DATABASE = "FlushID"
$env:GRAPHHOPPER_API_KEY = Read-Host "GraphHopper API key"
$env:GEMINI_API_KEY = Read-Host "Gemini API key"
```

To change the bundled defaults, edit `src/main/resources/environment_variables.env` and rebuild the JAR.

## Build and run from source

From the repository root:

```powershell
mvn clean test
mvn compile exec:java
```

You can also run `app.Main` from IntelliJ. The bundled configuration resource is already on the classpath; add environment variables to the run configuration only when you want to override its values.

## Build and run the executable fat JAR

The executable JAR is already committed to the repository. After cloning, run it directly without installing Maven or building from source:

```powershell
java -jar target\FlushID-1.0-SNAPSHOT.jar
```

Rebuild the JAR only after changing source code or `src/main/resources/environment_variables.env`:

```powershell
mvn clean package
```

The executable JAR is created at:

```text
target/FlushID-1.0-SNAPSHOT.jar
```

This is a self-contained fat JAR: it includes FlushID's runtime dependencies, bundled resources, configuration file, and `app.Main` entry point. It is the only `target/` artifact intentionally kept under version control; all other build output remains ignored.

## Data and startup behavior

At startup FlushID connects to the configured MongoDB database, loads the bundled set of named washrooms, and prepares database indexes. It also creates baseline review and hourly-status data for those known washrooms when missing. The application does not import the building and washroom collections from JSON, so the configured database must already contain them.

MongoDB stores users, reviews, review reports, status reports, and optional enrollment data. Passwords are stored as BCrypt hashes. The app supports legacy review fields (`stars`/`text`) as well as the current (`rating`/`comment`) form.

## Project structure

- `entity` - domain objects such as washrooms, reviews, routes, and status reports.
- `use_case` - application rules, input/output boundaries, and gateway ports.
- `interface_adapter` - controllers, presenters, and observable view models.
- `database` - MongoDB, GraphHopper, Gemini, and security adapters.
- `configuration` - bundled-configuration loading with process-environment overrides.
- `views` - Swing screens, dialogs, map rendering, and chart components.
- `app` - composition root, startup loading, and dependency wiring.
- `src/main/resources` - bundled data, image assets, and environment configuration.

## Testing and coverage

Run the test suite and generate the JaCoCo report with:

```powershell
mvn test
```

Open `target/site/jacoco/index.html` after the run to inspect coverage. The test suite uses deterministic fakes and local HTTP servers for application behavior and route/geocoding adapter coverage; it does not require live MongoDB, GraphHopper, or Gemini access.
