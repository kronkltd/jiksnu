FROM jiksnu

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

# Install Supervisor
RUN set -x \
    && apt-get -qy update \
    && apt-get -qy install supervisor \
    && rm -rf /var/lib/apt/lists/*

# Install ruby and bundler
RUN set -x \
    && apt-get -qy update \
    && apt-get -qy install bundler \
    && rm -rf /var/lib/apt/lists/*

# Install ruby packages
RUN bundle install

# Add Supervisor config
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Expose Karma port
EXPOSE 9876

ENTRYPOINT ["/usr/bin/supervisord"]
