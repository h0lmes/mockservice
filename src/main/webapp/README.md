### Web application

> Refer to Nuxt.js documentation for development guidelines.

To run web app in development mode (available on `localhost:3000` with hot reload):
    
    cd webapp
    npm install     // needs to be run once
    npm run dev

To build web app:

    cd webapp
    npm run all

This will copy web app into Thymeleaf template folder `src/main/resources/webapp`. 

Then you may build/run Spring Boot application.