#!/bin/sh

echo "Starting entrypoint script..."
echo "API_URL=$API_URL"

# Ensure the API_URL is replaced in runtime files if needed
if [ -n "$API_URL" ]; then
    echo "Replacing API_URL in runtime files..."
    # Replacing VE("${API_URL}") with the value of $API_URL
    sed -i "s|VE(\"${API_URL}\")|${API_URL}|g" /usr/share/nginx/html/*.js
else
    echo "No API_URL provided"
fi

# Run nginx with the passed command arguments
exec "$@"
