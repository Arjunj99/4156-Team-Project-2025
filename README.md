# 4156-Team-Project-2025 - Team Hello World
This is the GitHub repository for the **Team Project** associated with COMS 4156 Advanced Software Engineering.
Our group, **Hello World**, consists of the following members
- Arjun Somekawa (as7423)
- Jonathan Tavarez (jt3481)
- Songhee Beck (sb4446)
- Valentino Vitale (vv2343)

## Cloud URL

https://calorie-service-295107751003.us-east1.run.app

## Building and Running a Local Instance
In order to build and use our service you must install the following (This guide assumes MacOS but the Maven README has instructions for both Windows and Mac):

1. Maven 3.9.11: Run "mvn compile" and "mvn spring-boot:run". Running this command for the first time should install the necessary dependencies. If not use https://maven.apache.org/download.cgi to download and follow the installation instructions, be sure to set the bin as described in Maven's README according to instructions of your OS (see README).
2. JDK 17: This project used JDK 17 for development so that is what I recommend you use: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
3. IntelliJ IDE: I recommend using IntelliJ but you are free to use any other IDE that you are comfortable with: https://www.jetbrains.com/idea/download/
4. To Run the code, you will need to have Google Credentials set up to use the FireStore Database. For testing purposes, email (jt3481@columbia.edu) for the credentials, and he will send them to you. After receiving the credentials, export them using the code below. 

export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/service-account.json"

After this, the tests and service will work for you locally.

4. When you open IntelliJ you have the option to clone from a GitHub repo, click the green code button and copy the http line that is provided there and give it to your IDE to clone.
5. That should be it in order to build the project with maven you can run <code>mvn -B package --file pom.xml</code> and then you can either run the tests via the test files described below or the main application by running SweProjectApplication.java from your IDE.
6. If you wish to run the style checker you can with <code>mvn checkstyle:check</code> or <code>mvn checkstyle:checkstyle</code> if you wish to generate the report.
7. If you wish to run the unit tests (which can be found in /CalorieService/src/test/java/dev/coms4156/project/calorieservice), you can with <code>mvn clean test</code> and <code>mvn jacoco:report</code>, which generates a report in /CalorieService/target/site/jacoco/index.html.
8. If you wish to run static analysis of the code base, you can with <code>mvn pmd:check</code>.
9. All CI/CD development logic for Github Actions are set up in /.github/workflows/java-build.yml, which tests code pushed to main or dev branches of your repo.

# List of all Endpoints

## RouteController.java
#### /food/alternative
* HTTP Method: GET
* Expected Input Parameters: foodId (int)
* Expected Output: foodAlternatives (ResponseEntity<?>)
* Returns 5 random foods of the same food category with lower calorie count.
* Status Codes:
  * 200 OK: Successfully retrieved food alternatives or no alternatives found
  * 404 NOT FOUND: Food with specified ID not found
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /recipe/alternative
* HTTP Method: GET
* Expected Input Parameters: recipeId (int)
* Expected Output: recipeAlternatives (ResponseEntity<?>)
* Returns 3 recipes of same recipe category with lower calorie count and 
highest views, as well as 3 random recipes of the same category with lower calorie count.
* Status Codes:
  * 200 OK: Successfully retrieved recipe alternatives
  * 404 NOT FOUND: Recipe with specified ID not found
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /recipe/totalCalorie
* HTTP Method: GET
* Expected Input Parameters: recipeId (int)
* Expected Output: calorie (ResponseEntity<?>)
* Returns the estimated total calorie of a given recipe.
* Status Codes:
  * 200 OK: Successfully calculated total calories
  * 404 NOT FOUND: Recipe with specified ID not found
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /recipe/calorieBreakdown
* HTTP Method: GET
* Expected Input Parameters: recipeId (int)
* Expected Output: calorieBreakdown (ResponseEntity<?>)
* Returns a dict of each ingredient in recipe and estimated calorie value for that ingredient.
* Status Codes:
  * 200 OK: Successfully retrieved calorie breakdown
  * 404 NOT FOUND: Recipe with specified ID not found
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /client/recommend
* HTTP Method: GET
* Expected Input Parameters: clientId (int)
* Expected Output: recipes (ResponseEntity<?>)
* Returns a list of 10 recommended recipes based on client's liked recipes.
* Status Codes:
  * 200 OK: Successfully retrieved recommendations or no recommendations found
  * 404 NOT FOUND: Client with specified ID not found
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /client/recommendHealthy
* HTTP Method: GET
* Expected Input Parameters: clientId (int), calorieMax (int)
* Expected Output: recipes (ResponseEntity<?>)
* Returns a list of 10 recommended recipes based on client's liked recipes under the calorieMax value.
* Status Codes:
  * 200 OK: Successfully retrieved healthy recommendations or no recommendations found
  * 404 NOT FOUND: Client with specified ID not found
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /client/likeRecipe
* HTTP Method: POST
* Expected Input Parameters: clientId (int), recipeId (int)
* Expected Output: status (ResponseEntity<?>)
* Adds a recipe to client's likedRecipes.
* Status Codes:
  * 200 OK: Recipe successfully added to client's liked recipes
  * 400 BAD REQUEST: Client/recipe not found or recipe already liked
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /recipe/addRecipe
* HTTP Method: POST
* Expected Input Parameters: recipe (Recipe)
* Expected Output: status (ResponseEntity<?>)
* Adds a recipe to service.
* Status Codes:
  * 201 CREATED: Recipe successfully added
  * 400 BAD REQUEST: Recipe payload is invalid or recipe ID is missing
  * 409 CONFLICT: Recipe with specified ID already exists
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /food/addFood
* HTTP Method: POST
* Expected Input Parameters: food (Food)
* Expected Output: status (ResponseEntity<?>)
* Adds a food to service.
* Status Codes:
  * 200 OK: Food successfully added
  * 400 BAD REQUEST: Food object is null, invalid, or ID already exists
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /recipe/viewRecipe
* HTTP Method: POST
* Expected Input Parameters: recipeId (int)
* Expected Output: status (ResponseEntity<?>)
* Increment Recipe's Views.
* Status Codes:
  * 200 OK: Recipe view successfully recorded
  * 404 NOT FOUND: Recipe with specified ID not found
  * 500 INTERNAL SERVER ERROR: Server error occurred

#### /recipe/likeRecipe
* HTTP Method: POST
* Expected Input Parameters: recipeId (int)
* Expected Output: status (ResponseEntity<?>)
* Increment Recipe's likes.
* Status Codes:
  * 200 OK: Recipe like successfully recorded
  * 404 NOT FOUND: Recipe with specified ID not found
  * 500 INTERNAL SERVER ERROR: Server error occurred

## Client Application (Demo Client)
Our project includes a full React-based demo client located inside this repository at CalorieService/recipe-client/, as well as helper java files located in CalorieService/src/main/java/dev/coms4156/project/calorieservice/client, CalorieService/src/main/java/dev/coms4156/project/calorieservice/config and CalorieService/src/main/java/dev/coms4156/project/calorieservice/controller.

This client provides a graphical interface for interacting with many of the major API endpoint of the Calorie Service. It is used during local development, end-to-end testing, and during our final demo. It should be noted that this demo client serves as an example of what can be made using this service. While this client acts as a recipe recommendation site, another client could easily be made with another purpose, such as a restaurant menu calorie estimator.

### What the Client Does
The demo client allows a user to:
- Sign in with a local user ID
- Get recommended recipe recommendations based on calorie restrictions from the service
- View recipe details and ingredient calorie breakdowns
- Like recipes (which records likes both within the client data-store and in the service's DB)

The client communicates directly with the backend service deployed at: https://calorie-service-295107751003.us-east1.run.app/. All network calls are made using a lightweight proxy layer defined in CalorieService/recipe-client/src/App.jsx.

### How to Build and Run the Client
From the repository root, run:
1. cd CalorieService/recipe-client
2. npm install     (first time only)
3. npm run dev

Then open a browser to:
http://localhost:5173.

Your client is now connected to the backend service and ready for E2E testing.

### How Multiple Client Instances Are Distinguished
The service was explicitly designed to handle multiple simultaneous clients.
The demo client identifies itself to the server using three independent identifiers:

1. X-Client-Id: A static ID for the entire client application (example: 502).
This distinguishes different client types.
2. X-Instance-Id: A random UUID generated per browser installation and stored in localStorage.
This distinguishes multiple clients.
3. userId: A human-readable user identity meaningful to the UI (ex: "alice01").
It identifies which local person is currently interacting with the client.

Due to this, multiple instances can interact with the service simultaneously without interfering with each other, and logs clearly indicate which instance and user produced each call. You may use the test files as proof of this behavior: MultiClientLogTests.java and MultiClientRouteControllerTest.java.

### How to Test the Client
Our demo-client has a features to assist developers when it comes testing their code. Below is a list of the test files and how to run them.

#### CalorieService/recipe-client/src/App.test.jsx
CalorieService/recipe-client/src/App.test.jsx contains a thorough test file with tests for App.jsx. These tests are used to check as many plausible equivalence partitions for the Client front-end behavior. It is built using Jest.

You can run this test, alongside app.e2e.spec.js when you run <code>npm test</code> from the recipe-client directory. Reports are generated in /CalorieService/recipe-client/coverage/lcov-report/index.html.

#### CalorieService/recipe-client/tests/app.e2e.spec.jsx
CalorieService/recipe-client/tests/app.e2e.spec.jsx contains a test file with end-to-end tests for App.jsx. It is built using Playwright.

You can run the following steps to test the e2e test files from the recipe-client directory:
1. npx playwright install (first time only)
2. npx playwright test

Reports are generated in /CalorieService/recipe-client/test-results/.last-run.json.

These tests cover the following flows:
1. User can sign in end-to-end: Validates UI changes + /client/log.
2. Fetching healthy recipe recommendations: covers the /client/recommendHealthy endpoint.
3. Viewing recipe details: covers /recipe/viewRecipe and /recipe/calorieBreakdown.
4. Liking a recipe: Covers /client/likeRecipe functionality via LIKE button.
5. Multiple browser instances: Ensures different windows generate different instanceIds.
6. Backend error handling: Validates client error bar display on invalid inputs.

All tests communicate with the real client-backend and require both the service and the client to be running.

#### CalorieService/src/test/java/dev/coms4156/project/calorieservice
This directory contains many test files that test supporting features and capabilities found in ClientEvent.java, CoreConfig.java and ClientLogController.java (see start of this subsection). Corresponding test files are found in CalorieService/src/test/java/dev/coms4156/project/calorieservice and are automatically ran as part of the CI (see <code>mvn clean test</code>).

1. ClientEventTests.java: Tests the ClientEvent.java file.
2. ClientLogTests.java: Tests the backend component of the client which handles client-based logging. Note that /CalorieService/logs/client-events.log serves as a local persistent data store for the client.
3. MultiClientLogTests.java: Tests the behavior of a user querying the client (eg. using the client to log into their account the recipe site) and tests for multi-client behavior.
4. MultiClientRouteController.java: Tests the behavior of a client querying the service (eg. using the service to get recipes that meet the health criteria) and tests for multi-client behavior. Contains many equivalence partitions for Client -> Service Inputs.

## Third-Party Client Developer Guide
Any third-party developer can build their own client by following these guidelines.

### 1. API Base URL
All service endpoints are exposed at: https://calorie-service-295107751003.us-east1.run.app/

A third-party client can use any language or environment that supports HTTP calls.

### 2. Required Headers
Every request must include:
- X-Client-Id: <integer identifying the client application>
- X-Instance-Id: <unique ID per installation>

Clients may generate instance IDs however they choose (UUID recommended).

### 3. Authentication / User Context
Your service uses lightweight, client-managed identifiers:
- userId (query param)
- recipeId (query param)

There is no OAuth or authentication requirement.

### 4. Supported API Endpoints
Full endpoint documentation appears earlier in the README.
Third-party clients may implement any subset they require.

All responses use JSON.

### 5. Logging Requirement
All clients must POST to POST /client/log with a JSON body describing:
- instanceId
- serviceClientId
- userId
- timestamp
- action event (sign in, view, like, etc.)

This ensures observability across multiple client instances.

## Project Management Tools
We used a combination of Jira and a spreadsheet to keep track of tasks. These are the links to them.
- https://arjunsomekawa.atlassian.net/jira/software/projects/OPS/boards/1?atlOrigin=eyJpIjoiNWY5ZmRkNjQxMWEyNGI0Y2FmZjRjMzBiZWMwNmY0NWYiLCJwIjoiaiJ9
- https://docs.google.com/spreadsheets/d/1q3fpW9lZYhbR_of1h09LZfC2FTuzpdeuxaPmUfPsXw8/edit?usp=sharing

## Style Checking Report
I used the tool "checkstyle" to check the style of our code and generate style checking reports. Here is the report
as of the day of Dec 04, 2025. (These can be found in the reports folder):

![Screenshot of a checkstyle with no errors](reports/checkstyle_report.png)

Our Style Checking Ruleset is google_checks.xml.

## Branch Coverage Reporting
I used JaCoCo to perform branch analysis in order to see the branch coverage of the relevant code within the code base. See below
for screenshots demonstrating output.

![Screenshot of a code coverage report from the plugin](reports/jacoco_report.png)

## Static Code Analysis
I used PMD to perform static analysis on our codebase, see below for the most recent output.

![Screenshot of PMD analysis report](reports/mvd_report.png)

This image was captured on Dec 04, 2025.

## Jest Testing
I used Jest to perform end-to-end testing for our client's backend, see below for the most recent output.

![Screenshot of Jest analysis report](reports/npm_report.png)

This image was captured on Dec 04, 2025.

## Playwright Testing
I used Jest to perform end-to-end testing for our service, see below for the most recent output.

![Screenshot of Playwright analysis report](reports/npx_report.png)

This image was captured on Dec 04, 2025.

## Continuous Integration Report
This repository using GitHub Actions to perform continuous integration, to view the latest results go to the following link: https://github.com/Arjunj99/4156-Miniproject-2025-Students-part-3/actions

## Discussion of Endpoint Ordering
As a group, we discussed and decided that for our project, Endpoint Ordering does not matter.

## Postman Test Documentation
We used Postman to document and store our API endpoints/results. here is a link to access our API endpoints with sample responses (https://app.getpostman.com/join-team?invite_code=bc1fd36ae7da43f92c0d6aa188720c38c33af18b17aeaa68d339fffbe5decb95&target_code=5bd0b449f27ac1df88f67582412ba439).

## Tools used 
This section includes notes on tools and technologies used in building this project, as well as any additional details if applicable.

* Maven Package Manager
* GitHub Actions CI
    * This is enabled via the "Actions" tab on GitHub.
    * Currently, this just runs a Maven build to make sure the code builds on branch 'main'.
* Checkstyle
    * I use Checkstyle for code reporting. Note that Checkstyle does get run as part of the CI pipeline.
* PMD
    * I am using PMD to do static analysis of our Java code.
* JUnit
    * JUnit tests get run automatically as part of the CI pipeline.
* Spring Boot Test + MockMvc + Hamcrest
    * I use Spring Boot Test with MockMvc to exercise the HTTP endpoints end-to-end (RouteControllerTests, InternalIntegrationTests, ExternalIntegrationTests). My JSON assertions rely on Hamcrest matchers such as `hasItem`, `not`, and `containsString`. EndToEndTests run with `SpringBootTest(webEnvironment = RANDOM_PORT)` and `TestRestTemplate`, using Firestore credentials so they send real HTTP traffic through the stack.
* JaCoCo
    * I use JaCoCo for generating code coverage reports.
* Mockito
    * We used Mockito to mock our MockAPIService to do isolated testing of RouteController.
* Postman
    * We used postman to test that out API works.
* Playwright 
    * We used Playwright for automated end-to-end testing of the client against the deployed cloud backend. 
* Jest + React Testing Library 
    * Jest is used for unit testing the React client. 
* Vite 
    * We use Vite as the development server and bundler for the demo client.
