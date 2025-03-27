# Git Commit Renamer IDEA Plugin

An IntelliJ IDEA plugin that allows you to rename (amend) the most recent Git commit message easily.

## Features

- Adds a "Rename Current Commit" action to the Git menu and Git log context menu
- Verifies repository state to ensure the operation is safe
## Usage

### Building

This project uses Gradle with the Gradle IntelliJ Plugin:

```bash
./gradlew build
```

### Testing

To run the simple smoke tests:

```bash
./gradlew test
```

### Installation

To install the plugin locally:

1. Run `./gradlew buildPlugin`
2. Install the plugin from the zip file in `build/distributions`
