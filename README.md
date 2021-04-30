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
to supply data for each method (see file naming format below).

#
### File naming format

    METHOD_request_mapping.json


`METHOD` is any of HTTP methods.
Path variables are supported.
All path delimiters `/` should be substituted with an underscores `_`.

Only `.json` extension supported.

Example:

    @GetMapping("api/entity/{id})
    
        corresponds to
    
    GET_api_entity_{id}.json

> Important note. Except for HTTP method name (which should be in upper case)
all file names should be in lower case to avoid any errors
on case-sensitive (Unix-based) file systems.

#
### Data file contents format

By default a data file could contain any JSON.

Also it is possible to specify status code and additional headers at the beginning
or at the end of a file to override defaults (default status code is 200).

It is not possible to specify headers only.
Data file parser looks for `HTTP/1.1` at the beginning of a line
of a data file as an indication this file not only contains payload
but also status code and headers.

HTTP code and headers should be separated from the body with an empty line.

Example:

    HTTP/1.1 201
    Custom-Header: header value
    
    {"some_key": "some value"}
    
or

    {"message": "Internal error"}

    HTTP/1.1 400
    Custom-Header: header value

#
### Customizing response of an API method

You can include a header `Mock` in HTTP request.
There could be multiple options for different services in one header
separated by spaces.

There are two versions of this header:

    service_name/option
    
    ...or...
    
    service_name/endpoint_path/option
    
Example:

    Mock: AccountServiceController/option3 StoreServiceController/api_v1_item_{id}/option2

In the example above if you call an endpoint of the `AccountService`
a file with `#option3` before the extension would be loaded
(e.g. `resources/AccountServiceController/GET_accounts#option3.json`) regardless
of the endpoint path.

If you call an endpoint `/api/v1/item/{id}` of the `StoreService`
a file with `#option2` before the extension would be loaded
(e.g. `resources/StoreServiceController/GET_api_v1_item_{id}#option2.json`).

#
### Using predefined variables in JSON

In JSON files you can use predefined variables, those would be substituted
with their values each time an endpoint is fetched.

List of predefined variables (template functions):

- `${sequence}` - sequence of integers starting from 1
- `${random_int}` - random integer between 1 and 10 000
- `${random_int:min:max}` - random integer between `min` and `max`
- `${random_uuid}` - random UUID
- `${random_string}` - a string of 20 random characters in `[a-z]`
- `${random_string:min:max}` - a string of `min` to `max` random characters in `[a-z]`
- `${random_string_of:str1:str2:...}` - a random string of given arguments (may be useful to represent enum values)
- `${random_date}` - random date in yyyy-MM-dd format
- `${random_timestamp}` - random timestamp in yyyy-MM-dd HH:mm:ss.SSS format
- `${current_date}` - current date in yyyy-MM-dd format
- `${current_timestamp}` - current timestamp in yyyy-MM-dd HH:mm:ss.SSS format.

#
### Using provided variables in JSON

In JSON files you can use variables which are provided on a per-request basis.

Variables have the following format:

    ${var_name}
    
    ...or...
    
    ${var_name:default_value}

Mock engine makes available variables from the following sources:

1. Path variables. Example of mapping: `/api/v1/account/{id}`.
2. Request parameters. Example of request: `/api/v1/account?id=1`.
3. Request body (for non-GET requests). Body should contain JSON for this to work.
All fields of the JSON would be collected and made available as variables.
Hierarchy is being preserved (see example below).
4. `Mock-Variable` header (see section below).

Example of request body:

    {
        "key1": "value 1",
        "key2": {
            "key1": "other value 1"
        }
    }

The following variables would be created:

    ${key1}
    ${key2.key1}

#
### Mock-Variable header

You can include multiple headers `Mock-Variable` in HTTP request.
Each header defines exactly one variable.

There are two versions of this header:

    service_name/variable_name/value
    
    ...or...
    
    service_name/endpoint_path/variable_name/value
    
Example:

    Mock-Variable: AccountServiceController/total/1000
    Mock-Variable: StoreServiceController/api_v1_item_{id}/item_name/iPhone 12 Pro

In the example above if you call an endpoint of the `AccountServiceController`
a variable `total` with value `1000` would be available
regardless of the endpoint path.

If you call an endpoint `/api/v1/item/{id}` of the `StoreServiceController`
a variable `item_name` with value `iPhone 12 Pro` would be available.