# Build stage
FROM node:22-alpine AS build

# Install git to read version info
RUN apk add --no-cache git

WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
COPY backend/src/main/resources/data/ /app/backend/src/main/resources/data/

# Inject git version info as build-time env vars
ARG VITE_GIT_TAG
ARG VITE_GIT_COMMIT
ARG VITE_BUILD_TIME
ENV VITE_GIT_TAG=${VITE_GIT_TAG}
ENV VITE_GIT_COMMIT=${VITE_GIT_COMMIT}
ENV VITE_BUILD_TIME=${VITE_BUILD_TIME}

RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=build /app/frontend/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
