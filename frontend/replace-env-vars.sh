#!/bin/bash

# Replace the API_URL placeholder with the value from the environment variable
sed -i "s|\"apiUrl\": \".*\"|\"apiUrl\": \"$API_URL\"|g" ./src/environments/environment.ts
