#!/bin/sh

curl -XPUT 'http://elk:9200/_template/filebeat?pretty' -d@/etc/filebeat/filebeat.template.json
/etc/init.d/filebeat start
lein run
