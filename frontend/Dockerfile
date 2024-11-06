FROM node:20.18.0-slim AS build
WORKDIR /app

# Accept build argument
ARG API_URL

# Set the API_URL environment variable for the build process
ENV API_URL=$API_URL

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --prod

FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY ./config/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
