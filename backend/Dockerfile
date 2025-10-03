FROM node

WORKDIR /busbackend

COPY package*.json ./
RUN npm ci

COPY . ./

EXPOSE 2000

CMD ["node","src/server.js"]