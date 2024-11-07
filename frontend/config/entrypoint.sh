#!/bin/sh

# Replacement for the dev environment (targeting environment.development.ts)
if [ -n "$API_URL" ] && [ -f 'src/environments/environment.development.ts' ]; then
    echo "Replacing API_URL in environment.development.ts..."

    # Replacing the API URL in the development environment configuration
    sed -i "s|apiUrl: '.*',|apiUrl: '${API_URL}',|" src/environments/environment.development.ts
fi

# Replacement for the prod environment (targeting environment.ts)
if [ -n "$API_URL" ] && [ -f 'src/environments/environment.ts' ]; then
    echo "Replacing API_URL in environment.ts..."

    # Replacing the API URL in the production environment configuration
    sed -i "s|apiUrl: '.*',|apiUrl: '${API_URL}',|" src/environments/environment.ts
fi

# Replacement for the prod environment (targeting /usr/share/nginx/html/*.js files)
if [ -n "$API_URL" ] && [ -d '/usr/share/nginx/html' ]; then
    echo "Replacing API_URL in JavaScript files in /usr/share/nginx/html..."

    # Find and replace in JavaScript files in the production environment
    find '/usr/share/nginx/html' -type f -name "*.js" -exec sed -i "s|this.apiUrl=\"[^\"]*\"|this.apiUrl=\"${API_URL}\"|" {} \;
fi

# Execute the final command (e.g., Nginx start or any other command passed)
exec "$@"
