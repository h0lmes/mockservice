### Web application development

> Refer to Nuxt.js documentation for development guidelines.

To run web app in development mode (runs on port `3000` with hot reload):
    
    cd src/main/webapp
    npm install
    npm run dev

To build web app for deployment:

    cd src/main/webapp
    npm run all

This will copy web app distro into Thymeleaf template folder `src/main/resources/webapp`. 

Then you may build/run Spring Boot application.
It is set to run on 8081, so you may run it on the same machine
along with your application under test.