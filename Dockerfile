FROM gradle:8-jdk21 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN ./gradlew build

FROM mosipdev/openjdk-21-jre
WORKDIR /server
RUN mkdir -p /server/plugins
COPY --from=builder /home/gradle/project/testServer/*.jar /server/plugins/Freedom.jar
RUN sudo apt-get install curl
RUN curl -o server.jar https://fill-data.papermc.io/v1/objects/cfb9281c2657e21ecc8acdaa9efbd6b5b3e873fb5bac4c3b8ba4bba67aa13ee2/paper-26.1.2-65.jar
RUN echo "eula=true" > eula.txt
EXPOSE 25565
CMD ["java", "-Xmx2G", "-Xms2G", "-jar", "server.jar", "nogui"]
