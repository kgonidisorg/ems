# Use official Node.js 20 image
FROM node:20.19.2-slim AS builder

# Set working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy the rest of the application code
COPY . .

# Build the application
RUN sh -c "npm rebuild esbuild && npm run build"

# Use official Nginx image for serving static files
FROM nginx:1.28.0-alpine-slim

# Copy built files from the builder stage
COPY --from=builder /app/out /usr/share/nginx/html

# Set permissions and ownership for static files
RUN chmod -R 755 /usr/share/nginx/html && chown -R nginx:nginx /usr/share/nginx/html

# Expose port (change if your app uses a different port)
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]