# if [ -f "bin/lein" ]
# then
#     echo "lein already installed"
# else
#     wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
#     mkdir ~/bin
#     mv lein ~/bin/lein
#     chmod a+x ~/bin/lein
#     echo "export PATH=~/bin:$PATH" >> ~/.bashrc
#     . ~/.bashrc
# fi


echo "cd /vagrant" >> ~/.bashrc

cd /vagrant/checkouts/ciste
lein install

cd /vagrant/checkouts/ciste-incubator
lein install

cd /vagrant/checkouts/jiksnu-core
lein install

cd /vagrant/checkouts/jiksnu-command
lein install

cd /vagrant

npm install
bower install
lein cljsbuild once
lein lesscss once

# byobu-enable
