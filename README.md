### Mock service

A very simple to use yet extensible service to mock REST services.
Suitable for integration testing and similar use cases.

#
### Creating new controller

Given that new service name is `AccountService` do the following:
1. Create a new controller named `AccountServiceController`
extending `MockController` (there is no enforced convention for controller names,
you may name it as you like).
2. Create required methods in the controller (see `DemoServiceController`).
3. Create a new folder under `src/main/resources` folder.
Name the folder as your controller (`AccountServiceController` in this case).
4. Create data files under `src/main/resources/AccountServiceController` folder
to supply data for each method (see `DemoServiceController` folder).

#
### File naming format

    METHOD_request-mapping.json

Only `.json` extension supported.

`METHOD` is any of HTTP methods.

All standard path delimiters (`/`) should be substituted with an underscores (`_`).

Path variables supported.
Example: `api/entity/{id}` transforms into `api_entity_{id}`.

> Important note. File names should be in lower case,
except for HTTP method name which should be in upper case.

#
### Customizing HTTP status code and headers

You can specify status code and headers at the beginning
or at the end of a data file to override defaults (default status code is 200).

If you want to specify headers only you'd have to provide a status code as well.
Data file parser looks for 'HTTP/1.1' at the beginning of a line
of a data file as an indication this file not only contains payload
but also status code and headers.

HTTP code and headers should be separated from the body with an empty line.

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

You can include a header `Mock` in HTTP request.
There could be multiple options for different services in one header
separated by spaces.

There are two versions:
- service name / option
- service name / endpoint path / option.

Example:

    Mock: AccountService/option3 StoreService/api_v1_item_{id}/option2

In the example above if you call an endpoint of the `AccountService`
a file with `#option3` before the extension would be loaded
(e.g. `resources/AccountService/GET_accounts#option3.json`) regardless
of the endpoint path.

If you call an endpoint `/api/v1/item/{id}` of the `StoreService`
a file with `#option2` before the extension would be loaded
(e.g. `resources/StoreService/GET_api_v1_item_{id}#option2.json`).

#
### Using placeholders in JSON

In JSON files you can use placeholders, those would be substituted
with their values each time a file contents are fetched.

Those constants are:

- ${sequence:int} - sequence of integers starting from 1
- ${random:int} - random integer between 1 and 1 000 000
- ${random:uuid} - random UUID
- ${random:string} - a string of 20 random characters [a-z]
- ${random:date} - random date in yyyy-MM-dd format
- ${current:date} - current date in yyyy-MM-dd format
- ${current:timestamp} - current date and time in yyyy-MM-dd HH-mm-ss format.
