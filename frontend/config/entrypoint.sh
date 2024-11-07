#!/bin/sh

echo "Starting environment variable replacement in JavaScript files..."

if [ -n "$API_URL" ]; then
    echo "Replacing API_URL with value: $API_URL"

    # Perform the replacement and log the file contents
    find '/usr/share/nginx/html' -type f -name "*.js" -exec sed -i "s|\${API_URL}|$API_URL|g" {} \;

    # Check if replacement worked by printing out the contents of the first file
    echo "Contents of first JS file after replacement:"
    head -n 10 /usr/share/nginx/html/*.js  # Just print the first 10 lines of the first JS file
else
    echo "API_URL environment variable is not set!"
fi

# Start Nginx
exec "$@"
