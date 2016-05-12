FROM registry.kronkltd.net:5000/duck1123/jiksnu-ruby-base

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

RUN set -x \
    && apt-get update \
    && apt-get install -y \
       byobu \
    && rm -rf /var/lib/apt/lists/*

ADD Gemfile Gemfile.lock ${JIKSNU_HOME}/

RUN bundle install

### Add application
ADD . ${JIKSNU_HOME}/

CMD [ "script/docker" ]
