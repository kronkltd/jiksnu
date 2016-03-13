FROM pandeiro/lein:latest
MAINTAINER duck@kronkltd.net
ENV jiksnu_home /app
ENTRYPOINT []
WORKDIR ${jiksnu_home}

# Install nodejs
RUN set -x \
    && apt-get -y update \
    && apt-get -y install curl git build-essential \
    && curl -sL https://deb.nodesource.com/setup_5.x | bash - \
    && apt-get -y update \
    && apt-get -y install nodejs

# Pre-cache the deps
ADD project.clj package.json bower.json .bowerrc ${jiksnu_home}/
ADD script/ ${jiksnu_home}/script/
RUN script/bootstrap

ADD . ${jiksnu_home}/
EXPOSE 8080
CMD printenv; script/server
