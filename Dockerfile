FROM repo.jiksnu.org/duck1123/jiksnu-dev

MAINTAINER Daniel E. Renfer <duck@kronkltd.net>

### Bootstrap project
ADD project.clj package.json bower.json .bowerrc ${JIKSNU_HOME}/

ADD script ${JIKSNU_HOME}/script

RUN script/bootstrap

### Add application
ADD . ${JIKSNU_HOME}/

### Set start script
CMD [ "script/docker"]
