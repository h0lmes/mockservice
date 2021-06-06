### Mock Service

An easy to use service to mock REST and SOAP services.
Suitable for integration testing and similar purposes.

> Important: SOAP support is very simplistic and limited.
It requires you to provide valid SOAP envelope as a response.
There is NO support for WSDL/XSD.

#
### Where to start

Refer to README in `src/main/webapp` for instructions on how to build web application.

#
### Route Response

Response can contain any JSON or XML (any textual data actually).

You can specify HTTP status code and headers for REST endpoints
(not possible to specify headers only).
Parser looks for `HTTP/1.1` to start reading status code and headers.
Put empty line before body if headers go first.

Examples:

    HTTP/1.1 201
    Custom-Header: header value
    
    {"some_key": "some value"}
    
or

    {"message": "Internal error"}
    HTTP/1.1 400
    Custom-Header: header value

#
### Route Suffix

Suffixes allow you to create multiple responses for a single path.

To select a particular response send `Mock-Suffix` header in HTTP request.
Multiple `Mock-Suffix` headers supported per HTTP request.
Each header should define exactly one suffix.

Header format:

    request-mapping/suffix
    
Example:

    Mock-Suffix: api-v1-item-{id}/invalid_format

In the example above if you call an endpoint `/api/v1/item/{id}`
then the Route with the `invalid_format` suffix would match.

#
### Predefined variables

You can use predefined variables in Response, those would be substituted
with their values each time an endpoint is fetched.

List of predefined variables:

- `${sequence}` - sequence of integers starting from 1
- `${random_int}` - random integer between 1 and 10_000
- `${random_int:min:max}` - random integer between `min` and `max`
- `${random_long}` - random long between 1 and 1_000_000_000_000_000L
- `${random_long:min:max}` - random long between `min` and `max`
- `${random_uuid}` - random UUID
- `${random_string}` - a string of 20 random characters in `[a-z]`
- `${random_string:min:max}` - a string of `min` to `max` random characters in `[a-z]`
- `${random_date}` - random date in yyyy-MM-dd format
- `${random_timestamp}` - random timestamp in yyyy-MM-dd HH:mm:ss.SSS format
- `${current_date}` - current date in yyyy-MM-dd format
- `${current_timestamp}` - current timestamp in yyyy-MM-dd HH:mm:ss.SSS format.
- `${enum:str1:str2:...}` - a random one of given arguments (may be useful to represent enum values)

#
### Provided variables

You can use variables which are provided on a per-request basis.

Format:

    ${var_name}
    ${var_name:default_value}

Variables are collected from the following sources:

1. Path variables (example `api/v1/account/{id}`).
2. Request parameters (example `api/v1/account?id=1`).
3. Request payload (if it is in JSON).
All fields of the JSON would be collected and made available as variables
(preserving hierarchy, see example below).
4. `Mock-Variable` header (see section below).

Example of request payload:

    {
        "key1": "value 1",
        "key2": {
            "key1": "other value 1"
        }
    }

The following variables would be available:

    ${key1}
    ${key2.key1}

#
### Mock-Variable header

Multiple `Mock-Variable` headers supported per HTTP request.
Each header defines exactly one variable.

Header format:

    request-mapping/variable_name/value
    
Example:

    Mock-Variable: api-v1-item-{id}/item_name/Chips

In the example above if you call an endpoint `/api/v1/item/{id}`
a variable `item_name` with the value `Chips` would be available.