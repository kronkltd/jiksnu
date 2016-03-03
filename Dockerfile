FROM pandeiro/lein:latest
MAINTAINER duck@kronkltd.net
# RUN apt-get update && apt-get install lein
ADD . /app
CMD cd /app; script/server
