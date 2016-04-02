FROM clojure

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

ENV JIKSNU_HOME /app

ENTRYPOINT []

EXPOSE 8080

VOLUME /root/.m2

WORKDIR ${JIKSNU_HOME}

### Install build dependencies
RUN set -x \
    && apt-get -qy update \
    && apt-get -qy install curl git build-essential \
    && rm -rf /var/lib/apt/lists/*

### Install nodejs
RUN set -x \
    && curl -sL https://deb.nodesource.com/setup_5.x | bash - \
    && apt-get -qy install nodejs \
    && rm -rf /var/lib/apt/lists/*

### Install Filebeat
RUN set -x \
    && curl -L -O https://download.elastic.co/beats/filebeat/filebeat_1.0.1_amd64.deb \
    && dpkg -i filebeat_1.0.1_amd64.deb \
    && rm filebeat_1.0.1_amd64.deb

ADD filebeat.yml /etc/filebeat/filebeat.yml

### Add certs for Filebeat
# CA cert
# RUN mkdir -p /etc/pki/tls/certs
# ADD docker/elk/logstash-beats.crt /etc/pki/tls/certs/logstash-beats.crt

### Add notify-send proxy
RUN set -x \
    && curl -sL https://github.com/fgrehm/notify-send-http/releases/download/v0.2.0/client-linux_amd64 > /usr/bin/notify-send \
    && chmod +x /usr/bin/notify-send

ADD project.clj package.json bower.json .bowerrc ${JIKSNU_HOME}/
ADD script ${JIKSNU_HOME}/script

RUN script/bootstrap

ADD . ${JIKSNU_HOME}/

CMD [ "script/docker"]
