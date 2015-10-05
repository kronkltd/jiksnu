# -*- mode: ruby -*-
# vi: set ft=ruby :
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.network "forwarded_port", guest: 27017, host: 27017

  config.hostmanager.enabled = true
  config.hostmanager.manage_host = true
  config.hostmanager.ignore_private_ip = false
  config.hostmanager.include_offline = true
  config.hostmanager.ip_resolver = proc do |vm, resolving_vm|
    if vm.id
      `VBoxManage guestproperty get #{vm.id} "/VirtualBox/GuestInfo/Net/1/V4/IP"`.split()[1]
    end
  end

  config.vm.synced_folder "~/.m2", "/home/vagrant/.m2", owner: 'vagrant', group: 'vagrant'

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  config.ssh.forward_agent = true

  # config.vm.provision "shell", name: "base", path: "vagrant/provision.sh"

  config.vm.provision :chef_solo do |chef|
    chef.cookbooks_path = ["site-cookbooks", "cookbooks"]
    chef.roles_path = "roles"
    chef.data_bags_path = "data_bags"

    chef.add_recipe 'ack'
    chef.add_recipe 'emacs'
    chef.add_recipe 'git'
    chef.add_recipe 'java'
    chef.add_recipe 'lein'
    chef.add_recipe 'mongodb'
    chef.add_recipe 'nginx-proxy'
    chef.add_recipe 'nodejs'
    chef.add_recipe 'ssl_certificate'

    chef.json = {
      :nginx_proxy => {
        :proxies => {
          'jiksnu-dev' => {
            :port => 8080,
            :ssl_key => 'jiksnu-dev'
          }
        }
      },
      :nginx => {
        :default_site_enabled => false
      },
      :nodejs => {
        :npm_packages => [
          {
            :name => "bower"
          }
        ]
      },
      :ssl_certificate => {
        'jiksnu-dev' => {
          :common_name => 'jiksnu-dev'
        }
      },
      :java => {
        :jdk_version => '7'
      }
    }
  end

  config.vm.define :jiksnu, primary: true do |node|
    node.vm.hostname = 'jiksnu-dev'
    node.vm.network "private_network", type: "dhcp"

    node.vm.provision "shell", path: "vagrant/provision_vagrant.sh", privileged: false
    node.vm.provision "shell", path: "vagrant/start_server.sh"
  end
end
