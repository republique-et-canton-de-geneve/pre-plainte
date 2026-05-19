# kics-scan disable=fd54f200-402c-4333-a5a4-36ef6709af2f
ARG OCI_CATALOG_HOST=example.com
FROM $OCI_CATALOG_HOST/ch/ge/common/middlewares/java/ubi9-openjdk21:4.0.1-20250803_065025 AS maven

ARG APP_WORKDIR="/11729-PPEL-formulaire-api"
WORKDIR $APP_WORKDIR

COPY pre-plainte-rest/target/pre-plainte-rest-*.war $APP_WORKDIR/pre-plainte.war

EXPOSE 8080
HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/11729-PPEL-formulaire-api/pre-plainte.war"]
