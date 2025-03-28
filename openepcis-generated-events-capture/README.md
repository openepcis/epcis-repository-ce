# generated-events-capture


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:


```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.
You can then execute your native executable with: `./target/generated-events-capture-999-SNAPSHOT-runner.jar`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

### running

```shell
java -Dquarkus.rest-client.epcis-api.headers.API-KEY=<your-api-key> -Dquarkus.rest-client.epcis-api.headers.API-KEY-SECRET=<your-api-key-secret> -Dquarkus.rest-client.epcis-api.url=https://api.epcis.cloud|<your-repo-url>
```

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Command to generate Test Data from Jinja Template and send it to capture:

Here we will be including various timeout to avoid the Timeout exception that may occur during the generation and capture of large events (approx. 100000 or more serial numbers).
We may remove them for small events.

### Using the api-key and api-key-secret:

```bash
java -Dquarkus.rest-client.epcis-api.headers.GS1-Capture-Error-Behaviour=proceed \
     -Dquarkus.rest-client.epcis-api.headers.API-KEY=<api-key> \
     -Dquarkus.rest-client.epcis-api.headers.API-KEY-SECRET=<api-key-secret> \
     -Dquarkus.rest-client.epcis-api.url=<capture-url> \
     -Dquarkus.rest-client.epcis-api.read-timeout=36000000 \
     -Dquarkus.rest-client.epcis-api.connect-timeout=36000000 \
     -Dquarkus.http.read-timeout=36000000 \
     -jar target/generated-events-capture-999-SNAPSHOT-runner.jar \
     -t <path-to>/InputTemplate.json \
     -tc <path-to>/templateContent.json
```

### Using the Authorization token:

```bash
java -Dquarkus.rest-client.epcis-api.headers.GS1-Capture-Error-Behaviour=proceed \
     -Dquarkus.rest-client.epcis-api.headers.Authorization=Bearer\ <token> \
     -Dquarkus.rest-client.epcis-api.url=<capture-url> \
     -Dquarkus.rest-client.epcis-api.read-timeout=36000000 \
     -Dquarkus.rest-client.epcis-api.connect-timeout=36000000 \
     -Dquarkus.http.read-timeout=36000000 \
     -jar target/generated-events-capture-999-SNAPSHOT-runner.jar \
     -t <path-to>/InputTemplate.json \
     -tc <path-to>/templateContent.json
```

### To write to file and set the file to capture:

```bash
java -Dquarkus.rest-client.epcis-api.headers.GS1-Capture-Error-Behaviour=proceed \
     -Dquarkus.rest-client.epcis-api.headers.API-KEY=<api-key> \
     -Dquarkus.rest-client.epcis-api.headers.API-KEY-SECRET=<api-key-secret> \
     -Dquarkus.rest-client.epcis-api.url=<capture-url> \
     -Dquarkus.rest-client.epcis-api.read-timeout=36000000 \
     -Dquarkus.rest-client.epcis-api.connect-timeout=36000000 \
     -Dquarkus.http.read-timeout=36000000 \
     -jar target/generated-events-capture-999-SNAPSHOT-runner.jar \
     -t <path-to>/InputTemplate.json \
     -tc <path-to>/templateContent.json \
     -T
```

