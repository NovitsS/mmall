FROM ewolff/docker-java
COPY target/untitled.jar .
CMD /usr/bin/java -Xmx400m -Xms400m -jar untitled.jar