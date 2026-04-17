# Selenium Framework SDET

## Overview
This project is a Selenium-based test automation framework designed for Software Development Engineer in Test (SDET) roles. It provides a modular structure for managing test cases, utilities, and reports.

## Project Structure
```
selejava/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── framework/
│   │   │   │   │   ├── base/
│   │   │   │   │   │   └── BasePage.java
│   │   │   │   │   ├── constants/
│   │   │   │   │   │   └── AppConstants.java
│   │   │   │   │   ├── pages/
│   │   │   │   │   │   └── LoginPage.java
│   │   │   │   │   ├── utils/
│   │   │   │   │       ├── ConfigReader.java
│   │   │   │   │       ├── DriverManager.java
│   │   │   │   │       ├── ReportManager.java
│   │   │   │   │       ├── ScreenshotPdfReport.java
│   │   │   │   │       ├── ScreenshotUtil.java
│   │   │   │   │       ├── WaitUtil.java
│   │   │   │   │       └── WindowHelper.java
│   ├── test/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── framework/
│   │   │   │   │   ├── tests/
│   │   │   │   │       ├── AppTest.java
│   │   │   │   │       └── BaseTest.java
│   │   ├── resources/
│   │       ├── config.properties
│   │       ├── testng.xml
│   │       └── schemas/
├── test-output/
│   ├── reports/
│       └── TestReport.html
```

## Features
- **BasePage**: Provides common methods for interacting with web elements.
- **AppConstants**: Stores application-wide constants.
- **LoginPage**: Contains methods for login-related test cases.
- **Utilities**: Includes helper classes for configuration, driver management, reporting, and more.
- **Test Cases**: Organized under `tests` for modular and reusable test scripts.

## Prerequisites
- Java Development Kit (JDK) 8 or higher
- Maven 3.6+
- Selenium WebDriver
- TestNG

## Setup
1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```
2. Navigate to the project directory:
   ```bash
   cd SeleniumFrameworkSDET/selejava
   ```
3. Install dependencies:
   ```bash
   mvn clean install
   ```

## Running Tests
1. To execute all tests, run:
   ```bash
   mvn test
   ```
2. To execute specific tests, use:
   ```bash
   mvn -Dtest=TestClassName test
   ```

## Configuration
- Update `config.properties` in `src/test/resources` to set environment-specific configurations such as:
  - Base URL
  - Browser type
  - Timeout values

## Reporting
- Test execution reports are generated in the `test-output/reports` directory.
- Open `TestReport.html` in a browser to view detailed test results.

## Contributing
1. Fork the repository.
2. Create a new branch:
   ```bash
   git checkout -b feature-branch-name
   ```
3. Commit your changes:
   ```bash
   git commit -m "Description of changes"
   ```
4. Push to the branch:
   ```bash
   git push origin feature-branch-name
   ```
5. Open a pull request.

## License
This project is licensed under the MIT License. See the LICENSE file for details.