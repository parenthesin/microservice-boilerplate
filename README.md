# microservice-boilerplate
Clojure Microservice Boilerplate: http-kit, java-http-clj, ruuter, interceptor, schema, postgresql and tests. Made to be compiled in GraalVM 

## About this example
This source is a combination of two namespaces

 - **parenthesin**: Helpers and wrappers to give a foundation to create new services in clojure,
you can find components for database, http, webserver and tools for db migrations.
 - **microservice-boilerplate**: An example of how use the boilerplate, it's a simple btc wallet
that fetch the current btc price in USD and you can check your transaction history, do deposits and withdrawals.

Verb | URL                | Description
-----| ------------------ | ------------------------------------------------
GET  | /wallet/history    | get all wallet entries and current total
POST | /wallet/deposit    | do a deposit in btc in the wallet
POST | /wallet/withdrawal | do a withdrawal in btc in the wallet if possible

## Repl
To open a nrepl
```bash
clj -M:nrepl
```
To open a nrepl with all test extra-deps on it
```bash
clj -M:test:nrepl
```

## Run Tests
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
To generate a coverage report 
```bash
clj -M:test --plugin kaocha.plugin/cloverage
```

## Lint fix and format

```bash
clj -M:clojure-lsp format
clj -M:clojure-lsp clean-ns
clj -M:clojure-lsp diagnostics
```

## Migrations
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


## Docker
Start containers with postgres `user: postgres, password: postgres, hostname: db, port: 5432`  
and [pg-admin](http://localhost:5433) `email: pg@pg.cc, password: pg, port: 5433`
```bash
docker-compose -f docker/docker-compose.yml up -d
```
Stop containers
```bash
docker-compose -f docker/docker-compose.yml stop
```

## Running the server
First you need to have the database running, for this you can use the docker command in the step above.

### Repl
You can start a repl open and evaluate the file `src/microservice_boilerplate/server.clj` and execute following code:
```clojure
(start-system! (build-system-map))
```

### Uberjar
You can generate an uberjar and execute it via java in the terminal:
```bash
# genarate a service.jar in the root of this repository.
clj -X:uberjar
# execute it via java
java -jar target/service.jar
```

## Features

### System
- [schema](https://github.com/plumatic/schema) Types and Schemas
- [component](https://github.com/stuartsierra/component) System Lifecycle and Dependencies
- [http-kit](https://github.com/http-kit/http-kit) Http Server
- [ruuter](https://github.com/askonomm/ruuter) Http Routes System 
- [java-http-clj](https://github.com/schmee/java-http-clj) Http Client
- [interceptor](https://github.com/exoscale/interceptor) Http Client
- [cheshire](https://github.com/dakrone/cheshire) JSON encoding
- [aero](https://github.com/juxt/aero) Configuration file and enviroment variables manager
- [timbre](https://github.com/ptaoussanis/timbre) Logging library
- [next-jdbc](https://github.com/seancorfield/next-jdbc) JDBC-based layer to access databases
- [honeysql](https://github.com/seancorfield/honeysql) SQL as Clojure data structures
- [build-clj](https://github.com/seancorfield/build-clj) Common build tasks abstracted into a library

### Tests & Checks
- [kaocha](https://github.com/lambdaisland/kaocha) Test runner
- [kaocha-cloverage](https://github.com/lambdaisland/kaocha-cloverage) Kaocha plugin for code coverage reports
- [schema-generators](https://github.com/plumatic/schema-generators) Data generation and generative testing
- [state-flow](https://github.com/nubank/state-flow) Testing framework for integration tests
- [matcher-combinators](https://github.com/nubank/matcher-combinators) Assertions in data structures
- [pg-embedded-clj](https://github.com/Bigsy/pg-embedded-clj) Embedded PostgreSQL for integration tests
- [clojure-lsp](https://github.com/clojure-lsp/clojure-lsp/) Code Format, Namespace Check and Diagnosis

## Directory Structure
```
./
├── .clj-kondo -- clj-kondo configuration and classes
├── .github
│   └── workflows -- Github workflows folder.
├── docker -- docker and docker-compose files for the database
├── resources -- Application resources assets folder and configuration files.
│   └── migrations -- Current database schemas, synced on service startup.
├── src -- Library source code and headers.
│   ├── parenthesin -- Source for common utilities and helpers.
│   └── microservice_boilerplate -- Source for the service example (wallet).
└── test -- Test source code.
    ├── integration -- Integration tests source (uses state-flow).
    │   ├── parenthesin -- Tests for common utilities and helpers.
    │   └── microservice_boilerplate -- Tests for service example (wallet).
    └── unit -- Unity tests source (uses clojure.test).
        ├── parenthesin -- Tests for common utilities.
        └── microservice_boilerplate -- Tests for service example (wallet).
```

## Related

### Similar Projects
- [vloth/ts-microservice-boilerplate](https://github.com/vloth/ts-microservice-boilerplate)

### Forks
- [parenthesin/microservice-boilerplate](https://github.com/parenthesin/microservice-boilerplate)
- [parenthesin/microservice-boilerplate-malli](https://github.com/parenthesin/microservice-boilerplate-malli)

### Using this Template
- [rafaeldelboni/super-dice-roll-clj](https://github.com/rafaeldelboni/super-dice-roll-clj)

## License
This is free and unencumbered software released into the public domain.  
For more information, please refer to <http://unlicense.org>
