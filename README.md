## Set-up the OpenEPCIS-CE for capture and query

### Set-up:

1. Ensure you have stopped all old OpenEPCIS docker containers (if any running) from `docker-desktop` or via terminal:

```bash
docker stop $(docker ps -aq)
```

2. remove the `openepcis-kafka` container from `docker-desktop` or via terminal: 

```bash
docker rm openepcis-kafka
```

3. Ensure you have all the latest submodules/changes updated and in `main` branch:

```bash
git submodule update --init --recursive
git submodule foreach git checkout main
```

3. Navigate to folder `quarkus-rest-api-re` within `dist`:

```bash
cd dist
cd quarkus-rest-api-re
```

4. Execute the following in project to bring-up containers needed for OpenEPCIS:

```bash
docker compose up -d
```

5. Following contains should start running:

```
opensearch-dashboards
opensearch-node-01
quarkus-rest-api
openepcis-kafka
kafkasetup-1
```

5. Once the containers are up, you can check the logs of the `kafkasetup-1` container to see if everything is running smoothly and topics are created.

6. Check the logs for `quarkus-rest-api` container to see if the application is running smoothly. 

7. If the application is not running (i.e. not rebalacing to running) then it could be due to `quarkus-rest-api` started before the `kafkasetup-1`.

8. If so then you can restart the `quarkus-rest-api` container by executing the following command:

```
docker restart quarkus-rest-api
```

9. Now stop the `quarkus-rest-api`.

10. Navigate to `dist` folder in root and run the following command to bring-up the domain:

```bash
mvn clean quarkus:dev
```