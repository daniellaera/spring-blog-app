#!/bin/sh

# Ensure the API_URL environment variable is replaced in the HTML file
if [ -n "$API_URL" ]; then
    echo "Injecting API_URL into index.html"
    # Replace the placeholder with the actual API_URL in the index.html file
    sed -i "s|\${API_URL}|$API_URL|g" /usr/share/nginx/html/index.html
else
    echo "No API_URL provided"
fi

# Run nginx to serve the application
exec "$@"
