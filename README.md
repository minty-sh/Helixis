# Helixis - A Command-Line Utility Toolkit

Helixis is a versatile command-line interface (CLI) toolkit designed to provide a wide array of utilities for developers, system administrators, and anyone needing quick access to common cryptographic, networking, file manipulation, and data conversion tools.

## Features

- **Cryptography:** AES encryption/decryption, various hashing algorithms, and general cryptographic functions.
- **Networking:** DNS lookups, port scanning.
- **File Utilities:** Checksum calculation, file comparison, touching file timestamps.
- **Data Manipulation:** Date and time utilities, UUID generation, URL encoding/decoding, color conversions.
- **Password Management:** Secure password generation and analysis tools.

## Subcommands

Helixis is structured around subcommands, each addressing a specific category of tasks. Below is a list of available subcommands and their primary functions.

| Subcommand    | Description                                                                                             |
| :------------ | :------------------------------------------------------------------------------------------------------ |
| `aes`         | Provides functionalities for AES (Advanced Encryption Standard) encryption and decryption. Useful for securing sensitive data. |
| `checksum`    | Calculates and verifies checksums of files using various algorithms (e.g., MD5, SHA-1, SHA-256). Ensures file integrity. |
| `color`       | Offers utilities for color manipulation and conversion between different color formats (e.g., RGB, Hex, HSL). |
| `crypt`       | General cryptographic utilities, potentially including various encoding/decoding schemes or other cryptographic operations not covered by `aes` or `hash`. |
| `date`        | Provides a suite of tools for working with dates and times, including formatting, parsing, and time zone conversions. |
| `datediff`    | Calculates the difference between two specified dates or times, useful for age calculations or duration measurements. |
| `dns`         | Performs DNS (Domain Name System) lookups, allowing you to query DNS records for domains.               |
| `filecompare` | Compares the content of two files, highlighting differences. Essential for verifying file changes.        |
| `filetouch`   | Changes the access and/or modification timestamps of files, similar to the `touch` command in Unix-like systems. |
| `hash`        | Generates cryptographic hash values for input strings or files using algorithms like MD5, SHA-1, SHA-256, SHA-512, etc. |
| `password`    | Tools for generating strong, random passwords and potentially analyzing password strength.                |
| `portscan`    | Scans a target host for open ports, helping to identify network services running on a machine.            |
| `url`         | Utilities for URL encoding, decoding, and parsing, useful when dealing with web addresses and parameters. |
| `uuid`        | Generates universally unique identifiers (UUIDs) in various formats.                                    |

## Usage

To use Helixis, you typically invoke the main `helixis` command followed by the subcommand and its specific arguments. For example:

```bash
./gradlew run --args='hash --algorithm SHA-256 --text "Hello, World!"'
./gradlew run --args='dns --lookup example.com'
```

For detailed help on any subcommand, use the `--help` flag:

```bash
./gradlew run --args='hash --help'
```

## Installation

Helixis is a Java-based project built with Gradle. To get started, you'll need Java Development Kit (JDK) 21 or newer installed on your system.

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/minty-sh/Helixis.git
    cd Helixis
    ```

2.  **Build the project:**

    ```bash
    ./gradlew installDist
    ```

    This will compile the project and run tests. You can find the executable JAR in `app/build/libs/`.

3.  **Run the application:**

    You can run the application directly using Gradle:

    ```bash
    ./gradlew run --args='--help'
    ```

    Or, if you prefer to run the compiled JAR:

    ```bash
    ./app/build/install/app/bin/app --help
    ```

## License

This project is licensed under the [Apache-2.0 License](LICENSE).
