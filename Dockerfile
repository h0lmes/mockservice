FROM image

RUN mkdir /app

COPY ./target/mockservice-0.0.7.jar /app/

RUN chmod +x /app/run.sh

ENV LANG en_US.UTF-8

WORKDIR /app/
EXPOSE 8081

RUN groupadd -r app && useradd --no-log-init -r -g app app
USER app

CMD ["java", "-jar", "/app/mockservice-0.0.7.jar"]
