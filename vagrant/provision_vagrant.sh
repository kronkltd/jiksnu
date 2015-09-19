#!/bin/env bash

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
