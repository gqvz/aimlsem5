#!/bin/sh
# ProxyCommand=false prevents opening a new unauthenticated connection if the
# existing master has gone away. Recreate the bridge with a new mux in that case.
exec ssh -F /dev/null -S /ssh-mux -o BatchMode=yes -o ProxyCommand=false \
    -W 127.0.0.1:11434 zeb@10.25.1.1
