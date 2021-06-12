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
- [state-flow](https://github.com/nubank/state-flow) Testing framework for integration tests
- [matcher-combinators](https://github.com/nubank/matcher-combinators) Assertions in data structures
- [pg-embedded-clj](https://github.com/Bigsy/pg-embedded-clj) Embedded PostgreSQL for integration tests

## Usage

### Repl
This will build cmake files and download dependencies
```bash
clj -M:repl
```
### Run Tests
This will run all unit tests inside `./test/unit`
```bash
clj -M:u-test
```
This will run all integration tests inside `./test/integration`
```bash
clj -M:i-test
```
## License
This is free and unencumbered software released into the public domain.  
For more information, please refer to <http://unlicense.org/>
