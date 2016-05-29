FROM repo.jiksnu.org/duck1123/jiksnu-ruby-base

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

# Add Supervisor config
COPY docker/karma/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Expose Karma port
EXPOSE 9876

ADD Gemfile Gemfile.lock ${JIKSNU_HOME}/

# Install ruby packages
RUN bundle install

### Add application
ADD . ${JIKSNU_HOME}/

CMD ["/usr/bin/supervisord"]

# CMD [ "bash" ]
