# stage 1: build
FROM node:22-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build


# stage 2: run
FROM node:22-alpine

WORKDIR /app

RUN npm install -g serve

COPY --from=builder /app/dist/frontend/browser ./browser

# TLS certificate and key are mounted at runtime (see docker-compose.yml),
# never baked into the image.
RUN mkdir -p /app/certs

EXPOSE 4000

CMD ["serve", "-s", "browser", "-l", "4000", "--ssl-cert", "/app/certs/frontend.crt", "--ssl-key", "/app/certs/frontend.key"]
