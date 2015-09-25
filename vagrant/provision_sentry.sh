apt-get install -y python-virtualenv python-dev postgresql \
        postgresql-server-dev-9.3 redis-server libxml2-dev libxslt1-dev

sudo -u postgres createuser -s sentry
sudo -u postgres psql -c "alter user sentry with password 'sentry';"
# sudo nano  /etc/postgresql/9.3/main/pg_hba.conf
# local sentry md5
sudo service postgresql restart


sudo virtualenv /www/sentry/
sudo easy_install -UZ setuptools
sudo easy_install -UZ sentry[postgres]

sudo sentry init

# sudo nano .sentry/sentry.conf.py # Set postgres connection sentry / sentry

sudo -u postgres createdb -E utf-8 sentry

sentry --config=/root/.sentry/sentry.conf.py upgrade

# sentry --config=/root/.sentry/sentry.conf.py createsuperuser

# sentry --config=/root/.sentry/sentry.conf.py repair --owner=<username>

sentry --config=/root/.sentry/sentry.conf.py start
