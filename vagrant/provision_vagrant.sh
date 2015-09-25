#!/bin/env sh

if [ -f "bin/lein" ]
then
    echo "lein already installed"
else
    wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
    mkdir ~/bin
    mv lein ~/bin/lein
    chmod a+x ~/bin/lein
    echo "export PATH=~/bin:$PATH" >> ~/.bashrc
    . ~/.bashrc
fi


echo "cd /vagrant" >> ~/.bashrc

cd /vagrant/checkouts/ciste
~/bin/lein install

cd /vagrant/checkouts/ciste-incubator
~/bin/lein install

cd /vagrant/checkouts/jiksnu-core
~/bin/lein install

cd /vagrant/checkouts/jiksnu-command
~/bin/lein install

cd /vagrant

npm install
bower install
~/bin/lein cljsbuild once
~/bin/lein lesscss once

byobu-enable
