### Mock Service

An easy to use service to mock REST and SOAP services.
Suitable for integration testing and similar purposes.

> Important: SOAP support is very simplistic and limited.
It requires you to provide valid SOAP envelope as a response.
There is NO support for WSDL/XSD.

#
### Where to start

To create new endpoints:

1. Create new controller extending `BaseRestController` or `BaseSoapController`
(for example `AccountService`, there is no enforced convention
for controller names, controller name would mean group of routes).
2. Create required methods in the controller (see examples under `web/rest`
and `web/soap`).
3. Create a new folder under `src/main/resources/data` and
name it as your controller (`AccountService`).
4. Create data files under that folder (see file naming format below).

> Note. See example requests in `rest-api.http`.

#
### File naming format

    // for REST
    method-request-mapping.json
    or
    method-request-mapping--suffix.json
    
    // for SOAP
    request-mapping.xml 


Method is any of HTTP methods. Path variables supported.

Example:

    @GetMapping("api/entity/{id}")  ->  get-api-entity-{id}.json

#
### Data file contents

Data file can contain any JSON or XML (any textual data actually).

See examples in `resources/data` folder.

You can specify HTTP status code and headers in JSON files (not possible to specify headers only).
Data file parser looks for `HTTP/1.1` to start reading status code and headers.
Put empty line (or end of file) after headers.

Examples:

    HTTP/1.1 201
    Custom-Header: header value
    
    {"some_key": "some value"}
    
or

    {"message": "Internal error"}

    HTTP/1.1 400
    Custom-Header: header value

#
### Multiple data files per endpoint

Multiple `Mock-Suffix` headers supported per HTTP request.
Each header defines exactly one suffix.

Header format:

    request-mapping/suffix
    
Example:

    Mock-Option: api-v1-item-{id}/invalid_format

In the example above if you call an endpoint `GET api/v1/item/{id}`
then `get-api-v1-item-{id}--invalid_format.json` file would be loaded.

#
### Predefined variables

You can use predefined variables in data files, those would be substituted
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
- `${of:str1:str2:...}` - a random of given arguments (may be useful to represent enum values)
- `${random_date}` - random date in yyyy-MM-dd format
- `${random_timestamp}` - random timestamp in yyyy-MM-dd HH:mm:ss.SSS format
- `${current_date}` - current date in yyyy-MM-dd format
- `${current_timestamp}` - current timestamp in yyyy-MM-dd HH:mm:ss.SSS format.

#
### Provided variables

You can use variables which are provided on a per-request basis.

Variables have the following format in data file:

    ${var_name}
    ${var_name:default_value}

Service makes available variables from the following sources:

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

    Mock-Variable: api-v1-item-{id}/item_name/iPhone 12 Pro

In the example above if you call an endpoint `api/v1/item/{id}`
a variable `item_name` with value `iPhone 12 Pro` would be available.