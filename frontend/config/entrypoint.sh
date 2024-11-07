#!/bin/sh

echo "Starting environment variable replacement in JavaScript files..."

# Check if the API_URL is set and the target directory exists
if [ -n "$API_URL" ] && [ -d '/usr/share/nginx/html' ]; then
    echo "Replacing ${API_URL} in JavaScript files with value: $API_URL"

    # Replace the placeholder ${API_URL} with the actual value of the environment variable
    find '/usr/share/nginx/html' -type f -name "*.js" -exec sed -i "s|\${API_URL}|$API_URL|g" {} \;

    # Debug: Confirm the replacement by showing first few lines of a file
    echo "First few lines of the first .js file after replacement:"
    # Using $(find ...) to show the first file and its first 10 lines
    # shellcheck disable=SC2046
    head -n 10 $(find /usr/share/nginx/html -type f -name "*.js" | head -n 1)
else
    echo "API_URL is not set or target directory '/usr/share/nginx/html' does not exist!"
fi

# Execute the final command (e.g., Nginx start or any other command passed)
exec "$@"
