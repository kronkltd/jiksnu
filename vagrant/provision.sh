apt-get install -y openjdk-7-jdk emacs nginx mongodb git byobu npm

if [ -e "/usr/bin/node" ]
then
else
    ln -s /usr/bin/nodejs /usr/bin/node
fi
