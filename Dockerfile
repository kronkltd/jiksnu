FROM clojure
MAINTAINER duck@kronkltd.net
WORKDIR /app

# Install nodejs
RUN set -x \
    && apt-get -y update \
    && apt-get -y install curl git build-essential \
    && curl -sL https://deb.nodesource.com/setup_5.x | bash - \
    && apt-get -y update \
    && apt-get -y install nodejs

# Pre-cache the deps
ADD project.clj package.json bower.json .bowerrc /app/
ADD script/ /app/script/
# RUN script/bootstrap

# ENV JIKSNU_DB_NAME=${JIKSNU_DB_NAME:-jiksnu} JIKSNU_DB_HOST=${JIKSNU_DB_HOST:-mongo}
# ENV JIKSNU_DB_URL mongodb://${JIKSNU_DB_HOST}/${JIKSNU_DB_NAME}

ADD . /app/
EXPOSE 8080
CMD printenv; script/server
