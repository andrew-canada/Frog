# FlushID

FlushID is a Java Swing desktop application for finding University of Toronto washrooms. It combines an interactive campus map with washroom details, reviews, live crowd reports, directions, and personal planning.

## Contributors
(In Alphabetical Order)
 - Andrew
 - Eleanor
 - Ian
 - Mark
 - Sheena
   
## Table of Contents

 - [FlushID](#FlushID)
 - [Contributors](#Contributors)
 - [Table of Contents (links to here)](#Table-of-Contents)
 - [Features](#Features)
 - [Requirements](#Requirements)
 - [Configuration](#Configuration)
 - [Build and run from source](#build-and-run-from-source)
 - [Build and run the executable fat JAR](#build-and-run-the-executable-fat-jar)
 - [Data and startup behaviour](#data-and-startup-behavior)
 - [Project structure](#project-structure)
 - [Testing and coverage](#testing-and-coverage)
 - [Feedback](#feedback)
 - [Contribute to This Project](#contribute-to-this-project)
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

- JDK 24, matching `maven.compiler.release` in `pom.xml`. It can be installed from [here](https://www.oracle.com/ca-en/java/technologies/downloads/).
- Maven 3.9+ to build from source. It can be installed from [here](https://maven.apache.org/install.html).
- A graphical desktop environment with a monitor resolution of at least 1500x600 and internet access for the map and routing service.
- MongoDB database access containing the project washroom/building data.
- A GraphHopper API key. Gemini is required only when generating a personal plan.

## Configuration

The built JAR bundles `src/main/resources/environment_variables.env` and reads it at startup. The file supplies the MongoDB, GraphHopper, and Gemini values needed by the submission/demo build.

| Setting               | Required                | Purpose                                |
|-----------------------|-------------------------|----------------------------------------|
| `MONGODB_URI`         | Yes                     | MongoDB connection string.             |
| `MONGODB_DATABASE`    | No                      | Database name; defaults to `FlushID`.  |
| `GRAPHHOPPER_API_KEY` | Yes                     | Address lookup and walking directions. |
| `GEMINI_API_KEY`      | Only for personal plans | Gemini timetable-plan generation.      |

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

**IMPORTANT**: Please supply a fresh GEMINI_API_KEY to ``\Frog\src\main\resources\environment_variables.env`` and rebuild the JAR. Our Gemini API keys pushed to the repo keep either getting scraped or revoked by Google due to public exposure, causing the personal plan generation to fail.

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

## Data and startup behaviour

At startup FlushID connects to the configured MongoDB database, loads the bundled set of named washrooms, and prepares database indexes. It also creates baseline review and hourly-status data for those known washrooms when missing. The application does not import the building and washroom collections from JSON, so the configured database must already contain them.

MongoDB stores users, reviews, review reports, status reports, and optional enrollment data. Passwords are stored as BCrypt hashes. The app supports legacy review fields (`stars`/`text`) as well as the current (`rating`/`comment`) form.

## Project structure

- `entity` - domain objects such as washrooms, reviews, routes, and status reports.
- `use_case` - application rules, input/output boundaries, and gateway ports.
- `interface_adapter` - controllers, presenters, and observable view models.
- `database` - MongoDB, GraphHopper, Gemini, and security adapters.
- `configuration` - bundled-configuration loading with process-environment overrides.
- `views` - Swing screens, dialogues, map rendering, and chart components.
- `app` - composition root, startup loading, and dependency wiring.
- `src/main/resources` - bundled data, image assets, and environment configuration.

## Testing and coverage

With JDK 24 and Maven installed, run the full test suite and generate the JaCoCo coverage report from the repository root:

```powershell
mvn test
```

Open `target/site/jacoco/index.html` after the run to inspect coverage. The test suite uses deterministic fakes and local HTTP servers for application behaviour and route/geocoding adapter coverage; it does not require live MongoDB, GraphHopper, or Gemini access.
Maven writes the HTML report to `target/site/jacoco/index.html`; open that file in a browser to inspect overall, package, class, line, and branch coverage. The test suite uses deterministic fakes and local HTTP servers for application behavior and route/geocoding adapter coverage, so it does not require live MongoDB, GraphHopper, or Gemini access.

## Feedback

You can provide us feedback by DMing any member of the FlushID on GitHub.

## Contribute to This Project

Unfortunately, FlushID is not open to contributions as of now. If you would like to contribute, send a message to any one of the existing contributors. You are free to fork this repository, in conformance with the `LICENCE.md`.

