# Jiksnu ("zheek snoo")

[![Stack Share](http://img.shields.io/badge/tech-stack-0690fa.svg?style=flat)](http://stackshare.io/duck1123/jiksnu)
[![Build Status](http://build.jiksnu.org/job/jiksnu-core/branch/master/badge/icon)](http://build.jiksnu.org/job/jiksnu-core/branch/master/)
[![Stories in Ready](https://badge.waffle.io/duck1123/jiksnu.png?label=ready&title=Ready)](http://waffle.io/duck1123/jiksnu)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/duck1123/jiksnu?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

Jiksnu is a Lojban compound word (lujvo) for the words "Jikca
Casnu". This translates roughly to "is a social type of interaction"

## About Jiksnu

Jiksnu is a federated social network communicating over both HTTP and
XMPP. Currently, basic interop with Status.net servers is mostly
working. (Salmon support is not yet complete.) In addition, many other
standards-based applications work to some extent. The OneSocialWeb
protocol as well as other XEP-0277 XMPP-based servers are supported,
however, that code is out of date.

Jiksnu is built on top of the Ciste framework, and makes extensive use
of it's Action Routing framework and services.

## Developing

    sudo apt-get install -y vagrant
    vagrant plugin install vagrant-git
    vagrant plugin install vagrant-hostmanager

    git clone https://github.com/duck1123/jiksnu.git
    cd jiksnu/chef
    librarian-chef update
    cd ../
    vagrant up


## Thanks

I would like to thank the authors of the many excellent free software,
specifications and open services from which I have made use as libraries,
referred to as guides/instructions, poked to serve as reference
implementations, or simply looked to for inspiration. Without
their spirit of collaboration, none of this would ever have been
possible.

## License

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

### Resources

Loading icon courtesy of loading.io

## Donate

http://duck1123.tip.me
