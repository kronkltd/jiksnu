if [ -d "/vagrant/checkouts" ]
then
    echo "checkouts already created"
else
    mkdir /vagrant/checkouts
fi

if [ -d "/vagrant/checkouts/jiksnu-core" ]; then
    echo "jiksnu-core already loaded"
else
    cd /vagrant/checkouts
    git clone git@github.com:duck1123/jiksnu-core.git
fi

if [ -d "/vagrant/checkouts/octohipster" ]
then
    echo "Octohipster loaded"
else
    cd /vagrant/checkouts
    git clone git@github.com:duck1123/octohipster.git --branch edge
fi

if [ -d "/vagrant/checkouts/ciste" ]
then
    echo "ciste already loaded"
else
    cd /vagrant/checkouts
    git clone git@github.com:duck1123/ciste.git --branch develop
fi

if [ -d "/vagrant/checkouts/ciste-incubator" ]; then
    echo "ciste-incubator already loaded"
else
    cd /vagrant/checkouts
    git clone git@github.com:duck1123/ciste-incubator.git
fi

if [ -d "/vagrant/checkouts/jiksnu-command" ]; then
    echo "jiksnu-command already loaded"
else
    cd /vagrant/checkouts
    git clone git@github.com:duck1123/jiksnu-command.git
fi


cd /vagrant/checkouts/octohipster && lein install
cd /vagrant/checkouts/ciste && lein install
cd /vagrant/checkouts/ciste-incubator && lein install
cd /vagrant/checkouts/jiksnu-core && lein install
cd /vagrant/checkouts/jiksnu-command && lein install

cd /vagrant
./node_modules/.bin/bower install

lein run
