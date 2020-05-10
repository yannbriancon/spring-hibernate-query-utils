# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.3] - 2020-05-10
### Added
- Add back a more specific detection of N+1 queries due to missing lazy fetching.

### Changed
- Make the detection of N+1 queries due to missing eager fetching on the queries more specific.

## [1.0.2] - 2020-05-08
### Changed
- Rename N+1 query to plural.

### Removed
- [BUG FIX] Temporarily remove detection of eager fetching on queries to avoid false positives.

## [1.0.1] - 2015-05-06
### Changed
- [BUG FIX] Fix initialisation of ThreadLocal variables to avoid NullPointerExceptions.

## [1.0.0] - 2020-05-01
### Added
- Auto detection of N+1  queries
- Error level configuration by application properties

## [0.1.0] - 2019-12-18
### Added
- Feature to count the queries

[Unreleased]: https://github.com/yannbriancon/spring-hibernate-query-utils/compare/v1.0.3...HEAD
[1.0.3]: https://github.com/yannbriancon/spring-hibernate-query-utils/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/yannbriancon/spring-hibernate-query-utils/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/yannbriancon/spring-hibernate-query-utils/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/yannbriancon/spring-hibernate-query-utils/compare/v0.1.0...v1.0.0
[0.1.0]: https://github.com/yannbriancon/spring-hibernate-query-utils/tree/v0.1.0
