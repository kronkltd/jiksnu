FROM node:0.10

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

ENV SIGIL_URL https://github.com/gliderlabs/sigil/releases/download/v0.4.0/sigil_0.4.0_Linux_x86_64.tgz

RUN set -ex \
    && wget -q $SIGIL_URL -O sigil.tgz \
    && tar -zxv -C /usr/local/bin -f sigil.tgz \
    && rm sigil.tgz

RUN npm install -g \
      pump.io \
      databank-mongodb

ADD pump.io.json.tpl .

EXPOSE "80"

ADD bootstrap.sh .

CMD ["/bootstrap.sh"]
