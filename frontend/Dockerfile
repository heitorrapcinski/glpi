# Stage 1: Build the frontend application
FROM node:20-alpine AS build

WORKDIR /app

# Copy dependency manifests first for layer caching
COPY package.json package-lock.json ./

RUN npm ci

# Copy the rest of the source code
COPY . .

# Accept build arguments and set as environment variables for Vite
ARG API_GATEWAY_URL=/api
ARG APP_VERSION=1.2.0-SNAPSHOT
ARG PUBLIC_URL=/

ENV VITE_API_GATEWAY_URL=$API_GATEWAY_URL
ENV VITE_APP_VERSION=$APP_VERSION

RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy built static files from the build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
