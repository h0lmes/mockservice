![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

### Mock Service

An easy to use service to mock REST and SOAP endpoints.
Suitable for integration/e2e testing.
Supports importing routes from OpenAPI3/Swagger2 YAML files.

> Note: SOAP support is very simplistic and limited.
It requires you to provide a valid SOAP envelope as a response.
There is NO support for WSDL/XSD.

This project is licensed under the MIT License - see the [LICENSE](/LICENSE) file for details

#
### Where to start

Refer to [README](/src/main/webapp/README.md) in `src/main/webapp` for instructions on how to build web application.

#
### Route Response

Response can contain:
- response in textual format with or without HTTP head
(JSON or XML body)
- request in textual format **with** HTTP head
(JSON or XML body)

> See HTTP request and response formats here
https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages

**RESPONSE**

Response MUST go first.

If parser sees `HTTP/1.1` at the beginning of a line
it knows there are headers (are optional) in response.
After headers an empty line should go indicating end of HTTP head.
Then goes body if not blank.
> Anything that goes before HTTP head start
(before `HTTP/1.1`, or if head is not there at all)
is considered a response body as well.

**REQUEST** (optional)

Optional `---` may go between response and request for the sake of convenience.

Parser looks for `HTTP/1.1` at the end of a line
to start reading request.
Then headers may go (optional).
Then an empty line should go indicating head ended
(head is mandatory for request part).
Then goes body if not blank.

If request is present it would be executed asynchronously
after the response was sent.

**RESPONSE EXAMPLE** (body only)

    {"id": 1, name": "Johnny 5"}

**RESPONSE EXAMPLE** (with head)

    HTTP/1.1
    Cache-Control: no-cache
        
    {"code": "E000394", "message": "Internal error"}

**RESPONSE EXAMPLE** (head + body + request head + request body)

    HTTP/1.1
    Extra-Header: some data
    
    {
        "status": "PROCESSING",
        "id": "${item_id}"
    }
    ---
    POST /store/cart/item/${item_id} HTTP/1.1
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...
    
    {
        "status": "PROCESSED",
        "id": "${item_id}"
    }

#
### Request Validation

You can define **Request Body Schema** (in JSON format) for any Route
to have request body validated against the schema.

> See JSON schema homepage
https://json-schema.org/specification.html

Request body schema may also automatically come from OpenAPI 3 file
for imported routes.
Presence of schema would be indicated at import page.

If validation fails you would receive:
- a Route marked with Alt = '400' (this is the default option in Settings).
A special variable with error message would be available
to use in response (see Settings).
- or Error description (with `type` and `message` fields)

#
### Route Alt (Alternative)

Alt allow you to create alternative responses for the same path.

To select an Alt:
- send **Mock-Alt** header in HTTP request
- enable **Random Alt** in **Settings**
- or even **Go Quantum** 😄
- or use **Scenarios**.

Multiple **Mock-Alt** headers supported per HTTP request.
Each header should define exactly one alternative.
The first header whose path equals route path would be used.

Header format:

    request-mapping/alt
    
Example:

    Mock-Alt: api-v1-item-{id}/invalid_format

In the example above if you call an endpoint `/api/v1/item/{id}`
then the Route with the `invalid_format` Alt would match.

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

1. Path variables (`/api/v1/account/{id}`).
2. Request parameters (`/api/v1/account?id=1`).
3. Authorization JWT token. All fields of the payload JSON
would be collected as variables (preserving hierarchy).
4. Request payload (JSON). All fields of the JSON would be collected
as variables (preserving hierarchy).
5. Request payload (SOAP). All fields of the SOAP envelope Body tag
would be collected as variables
(without namespace, preserving hierarchy).
6. **Mock-Variable** header (see section below).

Example of request payload (JSON):

    {
        "key1": "value 1",
        "key2": {
            "key1": "other value 1"
        }
    }

The following variables would be available:

    ${key1}
    ${key2.key1}

Example of request payload (SOAP):

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="http://www.dataaccess.com/webservicesserver/">
        <soapenv:Header/>
        <soapenv:Body>
            <web:NumberToDollarsRequest>
                <web:Value>$74,383.56</web:Value>
            </web:NumberToDollarsRequest>
        </soapenv:Body>
    </soapenv:Envelope>

The following variables would be available:

    ${NumberToDollarsRequest.Value}

#
### Mock-Variable header

Multiple **Mock-Variable** headers supported per HTTP request.
Each header defines exactly one variable.

Header format:

    request-mapping/variable_name/value
    
Example:

    Mock-Variable: api-v1-item-{id}/item_name/Chips

In the example above if you call an endpoint `/api/v1/item/{id}`
a variable `item_name` with the value `Chips` would be available.