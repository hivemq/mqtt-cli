FROM openjdk:buster
WORKDIR /usr/app
COPY . .
RUN ./gradlew buildDebianPackage

FROM openjdk:slim
WORKDIR /usr/app

# strip the version because wildcards to not work with -jar
COPY --from=0 /usr/app/build/libs/mqtt-cli-*.jar mqtt-cli.jar

ENTRYPOINT [ "java", "-jar", "/usr/app/mqtt-cli.jar" ]
CMD [ "shell" ]
