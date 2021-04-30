### Mock service

A very simple to use yet extensible service to mock REST services.
Suitable for integration testing and similar use cases.

#
### Creating new controller

1. Create new controller extending `AbstractRestController` or `AbstractSoapController`
(for example `AccountServiceController`, there is no enforced convention
for controller names).
2. Create required methods in the controller (see example controllers in this project).
3. Create a new folder under `src/main/resources` folder and
name it as your controller (`AccountServiceController`).
4. Create data files under that folder (see file naming format below).

> Important note. SOAP support is somewhat limited and "crutchy". It requires
valid SOAP envelope in XML data files. There is NO support for WSDL/XSD.

#
### File naming format

    // for REST controllers
    METHOD_request_mapping.json
    
    // for SOAP controllers
    request_mapping.xml 


`METHOD` is any of HTTP methods.

Path variables supported in request mapping.

Example:

    @GetMapping("api/entity/{id})  ->  GET_api_entity_{id}.json

As you see path delimiters `/` substituted with an underscores `_`.

> Important note. Except for HTTP method name (which should be in upper case)
all file names should be in lower case to avoid any errors
on case-sensitive (Unix-based) file systems.

#
### Data file contents

Data file can contain any JSON or XML (any textual data actually).

You can specify HTTP status code and headers in JSON files (not possible to specify headers only).
Data file parser looks for `HTTP/1.1` to start reading status code and headers.
Put empty line (or end of file) after headers.

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

You can include `Mock` header in HTTP request. There are two formats of this header:

    service_name/option
    
    service_name/request_mapping/option
    
Example (multiple options separated with spaces):

    Mock: AccountServiceController/option3 StoreServiceController/api_v1_item_{id}/option2

In the example above if you call any endpoint of the `AccountServiceController`
then a file with `#option3` before the extension would be loaded
(e.g. `resources/AccountServiceController/GET_accounts#option3.json`).

And if you call an endpoint `api/v1/item/{id}` of the `StoreServiceController`
then `resources/StoreServiceController/GET_api_v1_item_{id}#option2.json` would be loaded.

#
### Using predefined variables

In data files you can use predefined variables, those would be substituted
with their values each time an endpoint is fetched.

List of predefined variables (template functions):

- `${sequence}` - sequence of integers starting from 1
- `${random_int}` - random integer between 1 and 10_000
- `${random_int:min:max}` - random integer between `min` and `max`
- `${random_long}` - random long between 1 and 1_000_000_000_000_000L
- `${random_long:min:max}` - random long between `min` and `max`
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

In data files you can use variables which are provided on a per-request basis.

Variables have the following format:

    ${var_name}
    
    ${var_name:default_value}

Mock engine makes available variables from the following sources:

1. Path variables. Example of mapping: `api/v1/account/{id}`.
2. Request parameters. Example of request: `api/v1/account?id=1`.
3. Request body if it contains JSON.
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

Multiple `Mock-Variable` headers supported per HTTP request.
Each header defines exactly one variable.

Header can have two formats:

    service_name/variable_name/value
    
    service_name/request_mapping/variable_name/value
    
Example:

    Mock-Variable: AccountServiceController/total/1000
    Mock-Variable: StoreServiceController/api_v1_item_{id}/item_name/iPhone 12 Pro

In the example above if you call any endpoint of the `AccountServiceController`
a variable `total` with value `1000` would be available.

And if you call an endpoint `api/v1/item/{id}` of the `StoreServiceController`
a variable `item_name` with value `iPhone 12 Pro` would be available.