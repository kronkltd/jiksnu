# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.network "forwarded_port", guest: 8080, host: 8080
  config.vm.network "forwarded_port", guest: 27017, host: 27017
  config.vm.network "forwarded_port", guest: 7888, host: 7888

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  config.ssh.forward_agent = true


  config.git.add_repo do |rc|
    rc.target = "git@github.com:duck1123/jiksnu-core.git"
    rc.path = "/vagrant/checkouts/jiksnu-core"
    rc.branch = "master"
    rc.clone_in_host = true
  end

  config.git.add_repo do |rc|
    rc.target = "git@github.com:duck1123/octohipster.git"
    rc.path = "/vagrant/checkouts/octohipster"
    rc.branch = "edge"
    rc.clone_in_host = true
  end

  config.git.add_repo do |rc|
    rc.target = "git@github.com:duck1123/ciste.git"
    rc.path = "/vagrant/checkouts/ciste"
    rc.branch = "edge"
    rc.clone_in_host = true
  end

  config.git.add_repo do |rc|
    rc.target = "git@github.com:duck1123/ciste-incubator.git"
    rc.path = "/vagrant/checkouts/ciste-incubator"
    rc.branch = "edge"
    rc.clone_in_host = true
  end

  config.git.add_repo do |rc|
    rc.target = "git@github.com:duck1123/jiksnu-command.git"
    rc.path = "/vagrant/checkouts/jiksnu-command"
    rc.branch = "edge"
    rc.clone_in_host = true
  end

  config.vm.provision "shell", path: "vagrant/provision.sh"
  config.vm.provision "shell", path: "vagrant/provision_vagrant.sh", privileged: false
end
