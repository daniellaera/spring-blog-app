FROM node:20.18.0-slim AS build
WORKDIR /app

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --prod

FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY ./config/nginx.conf /etc/nginx/conf.d/default.conf

COPY ./config/init.sh /usr/share/init.sh

# Ensure the init.sh script is executable
RUN chmod +x /usr/share/init.sh

EXPOSE 80

# Use entrypoint script
ENTRYPOINT ["/usr/share/init.sh"]
CMD ["nginx", "-g", "daemon off;"]
