# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [v1.1.0]
### Added
- Added 'chatmonitor' command with administration actions
- Added 'chatmonitor reload' command to reload configuration files without rebooting the server
- Added 'chatmonitor test [string]' command to enable testing of text without risking sending it through to the users
- Enable using color codes in user (or broadcast) responses in the config
- A 'mild' word config example.

### Fixed
- Utilize dependency shading

## [v1.0.1]
### Added
- This CHANGELOG.
- Example folder and a configuration file for common English swear words.
- An update-checker for the plugin, against spigotmc version.

### Changed
- README was adjusted with some images and examples for easier usage and installation

### Fixed
- Change some calls to backwards compatibility, allowing GoogleAction CI to run a backwards-compat build.

## [1.0.0] - 2020-01-02
- Initial official release.

[Unreleased]: https://github.com/mooeypoo/ChatMonitor/compare/v1.1.0...HEAD
[v1.1.0]: https://github.com/mooeypoo/ChatMonitor/compare/v1.0.1...v1.1.0
[v1.0.1]: https://github.com/mooeypoo/ChatMonitor/compare/v1.0.0...v1.0.1
[v1.0.0]: https://github.com/mooeypoo/ChatMonitor/releases/tag/v1.0.0