# Use official Node.js 20 image
FROM node:20.19.2-slim

# Set working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy the rest of the application code
COPY . .

# Build the application
RUN sh -c "npm rebuild esbuild && npm run build"

# Expose port (change if your app uses a different port)
EXPOSE 3000

# Start the application
CMD ["npm", "start"]