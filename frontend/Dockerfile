FROM node:20.18.0-slim AS build
WORKDIR /app

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --prod

FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY ./config/nginx.conf /etc/nginx/conf.d/default.conf

# Copy the entrypoint script for API_URL replacement
COPY ./config/entrypoint.sh /usr/share/entrypoint.sh
RUN chmod +x /usr/share/entrypoint.sh

EXPOSE 80
ENTRYPOINT ["/usr/share/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
