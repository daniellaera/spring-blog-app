#!/bin/sh

echo "Starting environment variable replacement in JavaScript files..."

if [ -n "$API_URL" ]; then
    echo "Replacing API_URL with value: $API_URL"
    find '/usr/share/nginx/html' -type f -name "*.js" -exec sed -i "s|\\${API_URL}|$API_URL|g" {} \;
else
    echo "API_URL environment variable is not set!"
fi

# Start Nginx
exec "$@"
