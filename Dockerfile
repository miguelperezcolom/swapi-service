# Runtime-only image from a locally-built jar — the same shape every service on this cluster uses.
# See deploy/build-image.sh; the jar is built outside so the image stays small and the build fast.
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app -u 10001 app
WORKDIR /opt/app
COPY --chown=app:app target/swapi-service-*.jar app.jar
# The container runs with a read-only root filesystem and only /tmp is writable, so the JVM has to
# be told where to put its scratch files.
ENV SERVER_PORT=8120 JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=/tmp
USER app
EXPOSE 8120
ENTRYPOINT ["java", "-jar", "app.jar"]
