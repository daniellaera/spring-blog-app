#!/bin/sh

# Ensure that API_URL is set in the environment variables
if [ -n "$API_URL" ]; then
    echo "Replacing API_URL in JavaScript files with value: $API_URL"

    # Replace ${API_URL} in all JS files in /usr/share/nginx/html with the actual value of API_URL
    find /usr/share/nginx/html -type f -name "*.js" -exec sed -i "s|\${API_URL}|$API_URL|g" {} \;

    # Optionally, print the first few lines of the first JS file to confirm
    echo "First few lines of the first JS file after replacement:"
    head -n 10 $(find /usr/share/nginx/html -type f -name "*.js" | head -n 1)
else
    echo "API_URL environment variable is not set!"
fi

# Run the default command (Nginx)
exec "$@"
