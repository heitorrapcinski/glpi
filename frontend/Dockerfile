# ─────────────────────────────────────────────────────────────────────────────
# GLPI PWA Frontend — Multi-stage Docker Build
#
# Build:   docker compose build frontend
# Run:     docker compose up frontend
# ─────────────────────────────────────────────────────────────────────────────

# ---- Build stage ----
FROM node:20-alpine AS build

WORKDIR /app

# Build-time environment variables
ARG API_GATEWAY_URL=/api
ARG APP_VERSION=0.0.0
ARG PUBLIC_URL=/

ENV VITE_API_GATEWAY_URL=$API_GATEWAY_URL
ENV VITE_APP_VERSION=$APP_VERSION
ENV VITE_PUBLIC_URL=$PUBLIC_URL

# Install dependencies first (layer caching)
COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts

# Copy source and build
COPY . .
RUN npm run build

# ---- Runtime stage ----
FROM nginx:1.27-alpine AS runtime

# Create non-root user
RUN addgroup -S glpi && adduser -S glpi -G glpi

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Adjust permissions for non-root execution
RUN chown -R glpi:glpi /usr/share/nginx/html && \
    chown -R glpi:glpi /var/cache/nginx && \
    chown -R glpi:glpi /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown glpi:glpi /var/run/nginx.pid

USER glpi

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
