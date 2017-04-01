FROM clojure

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

ENV JIKSNU_HOME /app
ENV JIKSNU_DB_HOST mongo
ENV HTTP_PORT 8080
WORKDIR ${JIKSNU_HOME}

ARG user=jiksnu
ARG group=jiksnu
ARG uid=1000
ARG gid=1000

### Create docker group
RUN groupadd -g ${gid} ${group} \
    && useradd -u ${uid} -g ${gid} --create-home -s /bin/bash ${user}

RUN set -x \
    && apt-get update \
    && apt-get install -y \
       byobu \
       supervisor \
       build-essential \
       curl \
       git \
       netcat \
    && rm -rf /var/lib/apt/lists/*

### Install nodejs
RUN set -x \
    && curl -sL https://deb.nodesource.com/setup_6.x | bash - \
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
ADD docker/web-dev/filebeat.yml       /etc/filebeat/filebeat.yml
ADD docker/web-dev/logstash-beats.crt /etc/pki/tls/certs/logstash-beats.crt

### Add notify-send proxy
RUN set -x \
    && curl -sL https://github.com/fgrehm/notify-send-http/releases/download/v0.2.0/client-linux_amd64 > /usr/bin/notify-send \
    && chmod +x /usr/bin/notify-send

# Add Supervisor config
COPY docker/web-dev/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

ADD script ${JIKSNU_HOME}/script
ADD package.json project.clj ${JIKSNU_HOME}/

RUN chown -R ${uid}:${gid} ${JIKSNU_HOME}

USER ${user}

RUN script/bootstrap

### Add application
ADD . ${JIKSNU_HOME}/

USER root

RUN chown -R ${uid}:${gid} ${JIKSNU_HOME}

USER ${user}

RUN script/update

# Expose Karma port
EXPOSE 9876
# Expose nRepl port
EXPOSE 7888
# Expose HTTP port
EXPOSE 8080

ENTRYPOINT []

# CMD ["/usr/bin/supervisord"]

CMD [ "script/entrypoint-run" ]
