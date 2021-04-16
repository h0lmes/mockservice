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

> Important note. File names should be in lower case, except for HTTP method name.

#
### Customizing HTTP status code and headers

Status code and headers could be specified at the beginning of the data file to override defaults (default status code is 200).

You can not override headers without overriding status code as well. Data file parser looks for 'HTTP/1.1' at the beginning of a line of a data file as an indication this file not only contains payload but status code and headers as well.

The HTTP code plus headers and the body should be separated with an empty line.

Example:

    HTTP/1.1 201
    Custom-Header: header value
    
    ... body content here ...
    
or

    ... body content here ...

    HTTP/1.1 400
    Custom-Header: header value

#
### Customizing response of an API method

One can include a header `Mock` along with HTTP request. Example:

    Mock: AccountService/option3 StoreService/api_v1_item_{id}/option2

In a two-parted version the text before the `/` symbol is a service name and the rest is an option name.
In the example above if you call an endpoint of the `AccountService` then a file with `#option3` before the extension would be loaded (e.g. `resources/AccountService/GET_accounts#option3.json`) regardless of the endpoint path.

In a three-parted version the middle part is an endpoint path (with slashes substituted by underscores like in file names).
In the example above if you call an endpoint `/api/v1/item/{id}` of the `StoreService` then a file with `#option2` before the extension would be loaded (e.g. `resources/StoreService/GET_api_v1_item_{id}#option2.json`).