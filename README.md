# microservice-boilerplate
Clojure Microservice Boilerplate: Components, Reitit, Pedestal, Schema, Postgresql and Tests

## Features

### System
- [schema](https://github.com/plumatic/schema) Types and Schemas
- [component](https://github.com/stuartsierra/component) System Lifecycle and Dependencies
- [pedestal](https://github.com/pedestal/pedestal) Http Server
- [reitit](https://github.com/metosin/reitit) Http Routes System 
- [clj-http](https://github.com/dakrone/clj-http) Http Client
- [aero](https://github.com/juxt/aero) Configuration file and enviroment variables manager
- [next-jdbc](https://github.com/seancorfield/next-jdbc) JDBC-based layer to access databases
- [honeysql](https://github.com/seancorfield/honeysql) SQL as Clojure data structures
- [depstar](https://github.com/seancorfield/depstar) Generates Uberjars for releases

### Tests
- [kaocha](https://github.com/lambdaisland/kaocha) Test runner
- [state-flow](https://github.com/nubank/state-flow) Testing framework for integration tests
- [matcher-combinators](https://github.com/nubank/matcher-combinators) Assertions in data structures
- [pg-embedded-clj](https://github.com/Bigsy/pg-embedded-clj) Embedded PostgreSQL for integration tests

### Linting
- [clj-kondo](https://github.com/clj-kondo/clj-kondo) Code linter
- [cljfmt](https://github.com/weavejester/cljfmt) Auto code formatting

## Usage

### Repl
To open a nrepl
```bash
clj -M:nrepl
```
To open a nrepl with all test extra-deps on it
```bash
clj -M:test:nrepl
```

### Run Tests
To run unit tests inside `./test/unit`
```bash
clj -M:test :unit
```
To run integration tests inside `./test/integration`
```bash
clj -M:test :integration
```
To run all tests inside `./test`
```bash
clj -M:test
```

### Lint
Auto code format
```bash
clj -M:lint-fix
```
Runs kondo to lint src/test files
```bash
clj -M:lint
```

### Migrations
To create a new migration with a name
```bash
clj -M:migratus create migration-name
```
To execute all pending migrations
```bash
clj -M:migratus migration
```
To rollback the latest migration
```bash
clj -M:migratus rollback
```
See [Migratus Usage](https://github.com/yogthos/migratus#usage) for documentation on each command.

### Docker
Start containers with postgres `user: postgres, password: postgres, hostname: db, port: 5432`  
and [pg-admin](http://localhost:5433) `email: pg@pg.cc, password: pg, port: 5433`
```bas
docker-compose -f docker/docker-compose.yml up -d
```
Stop containers
```bash
docker-compose -f docker/docker-compose.yml stop
```

## License
This is free and unencumbered software released into the public domain.  
For more information, please refer to <http://unlicense.org>
