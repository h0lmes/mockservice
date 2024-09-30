### Example web application

Before testing this example application run Mock Service in production mode (on port 8081).
To do this please refer to [Mock Service web application README](../../src/main/webapp/README.md).

After Mock Service has started navigate to its WebUI and to the `Config` section.
In the config plain text area paste contents of the following config file [config.yml](./config.yml) and hit `Save and apply`.
Now Mock Service is ready to serve mocked `products` API.

Then run this application in development mode (runs on port `3000` with hot reload):
    
    cd examples/webapp
    npm install
    npm run dev

