### Web application development

> Application stack is Nuxt 3 + Vue 3 + TypeScript.
> Refer to appropriate documentation for development guidelines.

To run tests (Vitest is used):

    npm.cmd test

To run web app in development mode:
    
    cd src/main/webapp
    npm install
    npm run dev

App runs on 127.0.0.1:3000 with hot reload.
Spring service expected on localhost:8081.
Frontend calls backend via runtime config / default dev base behavior.

The runtime env vars now supported in nuxt.config.ts:

    NUXT_PUBLIC_API_BASE
    NUXT_PUBLIC_WS_BASE

To build web app for deployment:

    cd src/main/webapp
    npm run all

**npm run all** copies the generated static app into
src/main/resources/webapp for Spring Boot serving (via Thymeleaf).

Then you may build/run Spring Boot application.
It is set to run on port 8081, so you can run it along
with your application under test which usually runs on 8080.

If you wish you can use Nuxt application separately, not leveraging Thymeleaf.
