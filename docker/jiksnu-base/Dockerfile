FROM clojure

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

ENV JIKSNU_HOME /app

WORKDIR ${JIKSNU_HOME}

### Install build dependencies
RUN set -x \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
       build-essential \
       curl \
       git \
       netcat \
    && rm -rf /var/lib/apt/lists/*

### Install nodejs
RUN set -x \
    && curl -sL https://deb.nodesource.com/setup_5.x | bash - \
    && apt-get install -y --no-install-recommends \
       nodejs \
    && rm -rf /var/lib/apt/lists/*

### Install Filebeat
RUN set -x \
    && curl -L -O https://download.elastic.co/beats/filebeat/filebeat_1.0.1_amd64.deb \
    && dpkg -i filebeat_1.0.1_amd64.deb \
    && rm filebeat_1.0.1_amd64.deb


### Add certs for Filebeat
# CA cert
ADD filebeat.yml /etc/filebeat/filebeat.yml
ADD logstash-beats.crt /etc/pki/tls/certs/logstash-beats.crt

### Add notify-send proxy
RUN set -x \
    && curl -sL https://github.com/fgrehm/notify-send-http/releases/download/v0.2.0/client-linux_amd64 > /usr/bin/notify-send \
    && chmod +x /usr/bin/notify-send

ENTRYPOINT []

EXPOSE 80
