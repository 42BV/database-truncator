# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [4.0.0] - 2018-12-20
### Changed
- Upgraded to spring-boot 4

## [1.0.0] - 2018-12-20
### Changed
- Upgraded to spring-boot 2.1.1 to be able to run with java 11

## [0.5.1] - 2018-03-19
### Added
- **Support for MariaDB and MySQL**; possibility to use MySQL or MariaDB. Both will make use of the same truncation algorithm: MariaDbTruncationStrategy.

## [0.5.0] - 2018-01-24
### Fixed
- Issue [#7](https://github.com/42BV/database-truncator/pull/7), **Speeded up Postgres truncation by combining queries**; the delete queries executed by the truncator have been merged into a single query, making the process of truncation slightly faster than before.