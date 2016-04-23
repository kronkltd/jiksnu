#!/bin/sh

# Generate config from environment variables
sigil -p -f pump.io.json.tpl | tee /etc/pump.io.json

pump
