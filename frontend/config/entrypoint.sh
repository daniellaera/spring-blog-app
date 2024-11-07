#!/bin/sh

# Ensure API_URL is available and replace it in the HTML/JS files before starting Nginx
if [ -n "$API_URL" ]; then
    echo "Replacing API_URL in the app with: $API_URL"

    # Replace the placeholder API_URL in JavaScript files
    find /usr/share/nginx/html -type f -name "*.js" -exec sed -i "s|\${API_URL}|$API_URL|" {} \;
fi

# Start Nginx in the foreground
exec "$@"
