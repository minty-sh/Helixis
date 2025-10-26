# Contributing to Helixis

We welcome contributions to Helixis! To ensure a smooth collaboration, please follow these guidelines.

## Commit Message Guidelines

We follow a convention for commit messages inspired by [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). This helps us maintain a clear and readable commit history.

Each commit message should be structured as follows:

```
<type>(<scope>): <subject>

[body]

[footer]
```

### Type

Must be one of the following:

* **feat**: A new feature
* **fix**: A bug fix
* **docs**: Documentation only changes
* **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semicolons, etc.)
* **refactor**: A code change that neither fixes a bug nor adds a feature
* **perf**: A code change that improves performance
* **test**: Adding missing tests or correcting existing tests
* **build**: Changes that affect the build system or external dependencies (example scopes: gradle, dependencies)
* **ci**: Changes to our CI configuration files and scripts (example scopes: github-actions)
* **chore**: Other changes that don't modify src or test files
* **revert**: Reverts a previous commit

### Scope (Optional)

The scope should indicate the part of the codebase affected by the change. Examples:

* `commands`
* `build`
* `docs`
* `deps`

### Subject

A very short, concise description of the change:

* Use the imperative mood ("add", "change", "fix")
* Do not capitalize the first letter
* No period at the end

### Body (Optional)

Use the body to provide additional contextual information about the code changes. Use the imperative mood.

### Footer (Optional)

Reference issues or pull requests that this commit addresses.

## Code Style Guidelines

We use Spotless and Checkstyle to enforce consistent code style. Please ensure your code adheres to these standards before submitting a pull request.

Key style rules include:

*   **Formatter**: Eclipse formatter with custom settings.
*   **Indentation**: 4 spaces for indentation.
*   **Line Length**: Maximum of 120 characters per line.
*   **Whitespace**: Trim trailing whitespace.
*   **Newlines**: Files must end with a newline.
*   **Imports**: Organized import order and removal of unused imports.

### Running Style Checks and Formatting

Before committing, you can run the following Gradle tasks to check and apply formatting:

* To check for style violations:
  ```bash
  ./gradlew spotlessCheck
  ```
* To automatically apply formatting fixes:
  ```bash
  ./gradlew spotlessApply
  ```

## Pull Request Guidelines

* Ensure your branch is up-to-date with the `main` branch.
* Create a pull request with a clear title and description.
* Ensure all tests pass and style checks are clean.
* One feature or bug fix per pull request.

Thank you for contributing!
