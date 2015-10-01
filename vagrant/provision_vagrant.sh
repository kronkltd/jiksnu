cd /vagrant/checkouts/ciste
lein install

cd /vagrant/checkouts/ciste-incubator
lein install

cd /vagrant/checkouts/jiksnu-core
lein install

cd /vagrant/checkouts/jiksnu-command
lein install

cd /vagrant

# npm install
bower install
lein uberjar
# lein run

echo "cd /vagrant" >> ~/.bashrc
byobu-launcher-install
