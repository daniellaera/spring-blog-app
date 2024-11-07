#!/bin/bash

# Ensure the API_URL is replaced in runtime files if needed
if [ -n "$API_URL" ]; then
    echo "Replacing API_URL in runtime files"
    sed -i "s|\"${API_URL}\"|\"$API_URL\"|g" /usr/share/nginx/html/*.js
fi

# Run nginx in the foreground
exec "$@"
