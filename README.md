### Mock service

A very simple to use yet extensible service to mock REST services.
Suitable for integration testing and similar use cases.

#
### Creating new controller

Given new service name is `AccountService` do the following:
1. Create a new controller named `AccountServiceController` and inject `MockService`.
2. Create required methods in the controller (see `DemoServiceController`).
3. Create a new folder named `AccountService` under `src/main/resources` folder. Note that folder name must be the controller ClassName without `Controller` suffix.
4. Create data files under `src/main/resources/AccountService` folder to supply data for each method.

#
### File naming format

    METHOD_request-mapping.json

Only `.json` extension supported.

`METHOD` is any of HTTP methods.

All standard path delimiters (`/`) should be substituted with an underscores (`_`).

Path variables supported. Example: `api/entity/{id}` transforms into `api_entity_{id}`.


#
### Customizing HTTP status code and headers

Status code and headers could be specified at the beginning of the data file to override defaults (default status code is 200).

You can not override headers without overriding status code as well. Data file parser looks for 'HTTP/1.1' at the beginning of the first line of a data file as an indication this file not only contains payload but status code and headers as well.

Headers and body should be separated with an empty line.

Example:

    HTTP/1.1 201
    Custom-Header: header value
    
    ... body content here ...
