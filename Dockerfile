FROM pandeiro/lein:latest
MAINTAINER duck@kronkltd.net
ENV jiksnu_home /app
ENTRYPOINT []
WORKDIR ${jiksnu_home}
EXPOSE 8080

# Install nodejs
RUN apt-get -y update && apt-get -y install curl git
RUN curl -sL https://deb.nodesource.com/setup_5.x | bash - && apt-get -y update && apt-get -y install nodejs

# Pre-cache the deps
ADD project.clj package.json bower.json .bowerrc ${jiksnu_home}/
ADD script/ ${jiksnu_home}/script/
RUN script/bootstrap

ENV JIKSNU_DB_NAME=${JIKSNU_DB_NAME:-jiksnu} JIKSNU_DB_HOST=${JIKSNU_DB_HOST:-mongo}
ENV JIKSNU_DB_URL mongodb://${JIKSNU_DB_HOST}/${JIKSNU_DB_NAME}

ADD . ${jiksnu_home}/
CMD printenv; script/server
