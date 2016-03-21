# FROM pandeiro/lein:latest
FROM clojure
MAINTAINER duck@kronkltd.net
ENV JIKSNU_HOME /app
ENTRYPOINT []
EXPOSE 8080
VOLUME /root/.m2
WORKDIR ${JIKSNU_HOME}

### Install build dependencies

RUN set -x \
    && apt-get -y update \
    && apt-get -y install curl git build-essential \
    && apt-get clean

### Install nodejs

RUN set -x \
    && curl -sL https://deb.nodesource.com/setup_5.x | bash - \
    && apt-get -y update \
    && apt-get -y install nodejs \
    && apt-get clean

### Install Filebeat

RUN curl -L -O https://download.elastic.co/beats/filebeat/filebeat_1.0.1_amd64.deb \
 && dpkg -i filebeat_1.0.1_amd64.deb \
 && rm filebeat_1.0.1_amd64.deb

ADD filebeat.yml /etc/filebeat/filebeat.yml

# CA cert
RUN mkdir -p /etc/pki/tls/certs
ADD logstash-beats.crt /etc/pki/tls/certs/logstash-beats.crt



ADD . ${JIKSNU_HOME}/
RUN script/setup

CMD "sh docker-bootstrap.sh"
