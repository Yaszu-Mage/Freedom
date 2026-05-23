FROM gradle:9-jdk-25-and-25 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN ./gradlew build

FROM eclipse-temurin:25-jammy
WORKDIR /server
RUN mkdir -p /server/plugins
COPY --from=builder /home/gradle/project/testServer/*.jar /server/plugins/Freedom.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /server/plugins && chmod 777 /server/plugins
RUN curl -o server.jar https://fill-data.papermc.io/v1/objects/cfb9281c2657e21ecc8acdaa9efbd6b5b3e873fb5bac4c3b8ba4bba67aa13ee2/paper-26.1.2-65.jar
RUN curl -o /server/plugins/PacketEvents.jar --max-time 30 --retry 3 https://cdn.modrinth.com/data/HYKaKraK/versions/ap8qHs7D/packetevents-spigot-2.12.1.jar && \
    curl -o /server/plugins/WorldEdit.jar --max-time 30 --retry 3 https://cdn.modrinth.com/data/1u6JkH5/versions/yDUBafTJ/worldedit-bukkit-7.4.3.jar && \
    curl -o /server/plugins/BetterModel.jar --max-time 30 --retry 3 https://cdn.modrinth.com/data/4h8rX3rt/versions/c3jAxql9/bettermodel-3.1.0-SNAPSHOT-498-paper.jar

RUN echo "eula=true" > eula.txt
EXPOSE 25565
CMD ["java", "-Xmx2G", "-Xms2G", "-jar", "server.jar", "nogui"]
