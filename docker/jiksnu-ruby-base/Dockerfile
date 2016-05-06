FROM duck1123/jiksnu-base

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

RUN set -x \
    && apt-get update \
    && apt-get install -y \
       supervisor \
       bundler \
    && rm -rf /var/lib/apt/lists/*
