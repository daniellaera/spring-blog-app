#!/bin/sh

# Check if API_URL is set and replace placeholder in production build files
if [ -n "$API_URL" ]; then
    echo "Replacing API_URL in production build files..."
    find '/usr/share/nginx/html' -type f -name "*.js" -exec sed -i "s|\${API_URL}|${API_URL}|g" {} \;
fi

exec "$@"
