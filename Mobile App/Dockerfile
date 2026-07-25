# Stage 1: Build the Expo/React Native app
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --legacy-peer-deps

COPY . .

# Build for web (Expo web is the only deployable target via Docker)
RUN npx expo export --platform web

# Stage 2: Serve with Nginx
FROM nginx:1.25-alpine

RUN apk add --no-cache curl

RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx config for SPA routing + API proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]