## Mock service

A very simple to use yet extensible service to mock REST services.
Suitable for integration testing and similar use cases.

### Creating new controller

Given new service name is `AccountService`:
1. Create a new controller named `AccountServiceController` and inject `MockService`.
2. Create required methods in the controller (see `DemoServiceController`).
3. Create a new folder named `AccountService` under `src/main/resources` folder. Note that folder name must be the controller ClassName without `Controller` suffix.
4. Create data files under `src/main/resources/AccountService` folder to supply data for each method.

### File naming format

    METHOD_request-mapping.json

In a `request-mapping` standard path delimiter must be substituted with `_`. It can contain standard placeholders like `{id}`.

`METHOD` is any of HTTP methods.