FROM nimmis/java-centos:openjdk-8-jdk
EXPOSE 8080
COPY target/cbrm-gui-0.0.1-SNAPSHOT.jar /app/cbrm-gui-0.0.1-SNAPSHOT.jar
COPY src/main/resources/install_flora.sh /flora/install_flora.sh
ADD https://sourceforge.net/projects/flora/files/latest/download?source=typ_redirect /flora/flora2.run
RUN ["chmod", "+x", "/flora/flora2.run"]
RUN ["chmod", "+x", "/flora/install_flora.sh"]
ENTRYPOINT ["/bin/sh"]
CMD ["/flora/install_flora.sh"]