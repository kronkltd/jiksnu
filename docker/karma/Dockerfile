FROM jiksnu-ruby-base

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

# Install ruby packages
RUN bundle install

# Add Supervisor config
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Expose Karma port
EXPOSE 9876

ENTRYPOINT ["/usr/bin/supervisord"]
