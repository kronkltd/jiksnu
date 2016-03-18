# FROM pandeiro/lein:latest
FROM clojure
MAINTAINER duck@kronkltd.net
ENV jiksnu_home /app
ENTRYPOINT []
WORKDIR ${jiksnu_home}

# Install nodejs
RUN set -x \
    && apt-get -y update \
    && apt-get -y install curl git build-essential

RUN set -x \
    && curl -sL https://deb.nodesource.com/setup_5.x | bash - \
    && apt-get -y update

RUN set -x \
    && apt-get -y install nodejs
    # \
    # && ln -s /usr/bin/nodejs /usr/bin/node

# RUN npm cache clean
# RUN npm install -g n
# RUN n stable
# RUN curl -L https://npmjs.org/install.sh | sh


# Pre-cache the deps
# ADD project.clj package.json bower.json .bowerrc ${jiksnu_home}/
# ADD script/ ${jiksnu_home}/script/
VOLUME /root/.m2
ADD . ${jiksnu_home}/
RUN script/setup

EXPOSE 8080
CMD lein run
