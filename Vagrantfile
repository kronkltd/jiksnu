# -*- mode: ruby -*-
# vi: set ft=ruby :
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.network "private_network", type: "dhcp"

  config.hostmanager.enabled = true
  config.hostmanager.manage_host = true
  config.hostmanager.ignore_private_ip = false
  config.hostmanager.include_offline = true
  config.hostmanager.ip_resolver = proc do |vm, resolving_vm|
    if vm.id
      `VBoxManage guestproperty get #{vm.id} "/VirtualBox/GuestInfo/Net/1/V4/IP"`.split()[1]
    end
  end

  # config.vm.network "forwarded_port", guest: 8080, host: 8080
  # config.vm.network "forwarded_port", guest: 27017, host: 27017
  # config.vm.network "forwarded_port", guest: 7888, host: 7888
  # config.vm.network "forwarded_port", guest: 9000, host: 9000

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  config.ssh.forward_agent = true

  # config.vm.provision "shell", name: "base", path: "vagrant/provision.sh"

  config.vm.provision :chef_solo do |chef|
    chef.cookbooks_path = ["site-cookbooks", "cookbooks"]
    chef.roles_path = "roles"
    chef.data_bags_path = "data_bags"

    chef.add_recipe 'mongodb'
    chef.add_recipe 'java'
    chef.add_recipe 'lein'
    # chef.add_recipe 'nginx'
    chef.add_recipe 'nodejs'
    # chef.add_recipe 'bower'
    # chef.add_recipe 'application'
    # chef.add_recipe 'application_nginx'
    chef.add_recipe 'ack'
    # chef.add_recipe 'application_java'
    chef.add_recipe 'emacs'
    chef.add_recipe 'git'
    chef.add_recipe 'nginx-proxy'

    # chef.application '/opt/jiksnu' do

    #   owner 'root'
    #   group 'root'

    #   nginx_load_balancer do
    #     only_if { node['roles'].include?('jiksnu_load_balancer') }
    #   end
    # end

    # chef.nginx_site "jiksnu" do
    #   host "jiksnu"
    #   # custom_data {
    #   #   :env => 'dev'
    #   # }
    # end

    chef.json = {
      :nginx_proxy => {
        :proxies => {
          'jiksnu-dev' => 8080
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
      :java => {
        :jdk_version => '7'
      }
    }
  end

  config.vm.define :jiksnu, primary: true do |node|
    node.vm.hostname = 'jiksnu-dev'


    node.vm.provision "shell", name: "jiksnu-local", path: "vagrant/provision_vagrant.sh", privileged: false
  end

  # config.vm.define :sentry do |node|
  #   node.vm.hostname = 'sentry'
  #   # node.vm.provision "shell", name: "sentry", path: "vagrant/provision_sentry.sh"
  # end
end
