# -*- mode: ruby -*-
# vi: set ft=ruby :
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"

  config.hostmanager.enabled = true
  config.hostmanager.manage_host = true
  config.hostmanager.ignore_private_ip = false
  config.hostmanager.include_offline = true
  config.hostmanager.ip_resolver = proc do |vm, resolving_vm|
    if vm.id
      `VBoxManage guestproperty get #{vm.id} "/VirtualBox/GuestInfo/Net/1/V4/IP"`.split()[1]
    end
  end
  config.ssh.forward_agent = true

  # Mount the user's maven cache to speed up boot
  config.vm.synced_folder "~/.m2", "/home/vagrant/.m2", owner: 'vagrant', group: 'vagrant'

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  config.vm.provision :shell,
                      path: 'https://github.com/fgrehm/notify-send-http/raw/master/vagrant-installer.sh',
                      args: ['12345']

  config.vm.define config.user.host_name, primary: true do |node|
    node.vm.hostname = config.user.host_name

    node.vm.network "private_network", type: "dhcp"
    # node.vm.network "forwarded_port", guest: 27017, host: 27017

    config.vm.provision :chef_solo do |chef|
      chef.cookbooks_path = "chef/cookbooks"
      chef.roles_path = "chef/roles"
      chef.data_bags_path = "chef/data_bags"

      chef.add_role "develop"
      chef.add_role "web"
      chef.add_role "db"

      chef.json = {
        "nginx_proxy" => {
          "proxies" => {
            node.vm.hostname => {
              "port" => 8080,
              "ssl_key" => node.vm.hostname,
              "location_config" => [
                "proxy_http_version 1.1;",
                "proxy_set_header Upgrade $http_upgrade;",
                'proxy_set_header Connection "upgrade";'
              ]
            }
          }
        },
        "nginx" => {
          "default_site_enabled" => false
        },
        "ssl_certificate" => {
          "items" => [
            {
              'name' => node.vm.hostname,
              "common_name" => node.vm.hostname,
            }
          ]
        }
      }
    end

    node.vm.provision "shell", path: "vagrant/provision_vagrant.sh", privileged: false
  end
end
