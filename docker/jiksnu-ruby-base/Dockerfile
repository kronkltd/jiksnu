FROM jiksnu-base

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

# Install Supervisor
RUN set -x \
    && apt-get update \
    && apt-get install -y \
       supervisor \
    && rm -rf /var/lib/apt/lists/*

# Install ruby and bundler
RUN set -x \
    && apt-get update \
    && apt-get install  -y \
       bundler \
    && rm -rf /var/lib/apt/lists/*
