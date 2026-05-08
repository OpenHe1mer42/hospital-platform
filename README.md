# Hospital Platform

Java 21 / JavaFX hospital platform project.

## Requirements

- JDK 21
- Maven 3.8+

## Build

```sh
mvn clean package
```

## Run

```sh
mvn javafx:run
```

## Database configuration

This project has a local MariaDB setup on `127.0.0.1:3307` with a passwordless
`root` user and a `carely` database.

Start it with:

```sh
scripts/start-local-mariadb.sh
```

The default `src/main/resources/database.properties` already points to:

```properties
db.url=jdbc:mariadb://127.0.0.1:3307/carely
db.username=root
db.password=
```

The app uses `carely.config.DatabaseConfig` as the shared
connection module. Any repository or service that needs the database should call:

```java
Connection connection = DatabaseConfig.getConnection();
```

Local credentials can be kept in `.env`:

```dotenv
DB_URL=jdbc:mariadb://127.0.0.1:3307/carely
DB_USERNAME=root
DB_PASSWORD=
```

Actual environment variables override `.env`, and `.env` overrides
`database.properties`.

You can also override database access with shell environment variables:

```sh
export DB_URL="jdbc:mariadb://127.0.0.1:3307/carely"
export DB_USERNAME="root"
export DB_PASSWORD=""
```

The Maven setup includes runtime JDBC drivers for PostgreSQL, MySQL, and MariaDB.
