![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)
[![License](.github/badges/license.svg)](/LICENSE)

# Mock Service

An easy-to-use service to mock REST and SOAP endpoints.
Suitable for integration and e2e testing.
Can import OpenAPI 3 and Swagger 2 formats (YAML).

> Note. SOAP support is very simplistic and limited.
You are required to provide a valid SOAP envelope as a response
(see an example later in this README).
There is NO support for WSDL/XSD.

This project is licensed under the MIT License -
see the [LICENSE](/LICENSE) file for details

# Where to start

1. Grab latest release binary.
2. Run it (Java 17+).
3. Navigate to `http://your_ip:8081`.
4. If you find UI unintuitive - contact me 🙃.

For build instructions refer to [README](/src/main/webapp/README.md).

# Development

To run the app

    $ ./mvnw

To build the app (given frontend app has been built)

    $ ./mvnw clean package

Generate code coverage report

    $ ./mvnw test


# Route response

Response field may contain:
- **response** wtih JSON or XML body, with or without HTTP head
- **callback request** with JSON or XML body, strictly **with** HTTP head

> See examples below and HTTP request and response format at
https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages

Response part MUST go first.

If a callback request used a delimiter `---` should go
between response and callback request.

If callback request is present it would be executed asynchronously
2 seconds after response is sent back.

**EXAMPLES**

Response (body only):

    {"id": 1, name": "Johnny 5"}

Response with head:

    HTTP/1.1
    Cache-Control: no-cache
        
    {"code": "E000394", "message": "Internal error"}

Response head + body + callback request head + body:

    HTTP/1.1
    Extra-Header: arbitrary header value
    
    {
        "status": "PROCESSING",
        "id": "${item_id}"
    }
    ---
    POST https://backend.cool-store.com/store/cart/item/${item_id} HTTP/1.1
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...
    
    {
        "status": "PROCESSED",
        "id": "${item_id}"
    }

# Route alt

**Alt** allows creating multiple responses
for the same mapping (e.g. method + path).
By default, alt is empty.

**Alt** can be a condition. If Alt contains `=` then it wll be parsed and compared
with **Request variables** (see section below). If a match is found - this
route is used to get a response. Only one variable in a condition is supported.

Examples:

    key1 = 1
    user_data.first_name = "Albert Einstein"

Apart from that to select specific alt you can also:
- either send **Mock-Alt** header in HTTP request
- or use **Scenarios**.

Multiple **Mock-Alt** headers supported per HTTP request.
One header should define exactly one alt.
The first header whose path equals route path would be used.

**Mock-Alt** header format:

    request-mapping/alt

**Mock-Alt** header example:

    Mock-Alt: api-v1-item-{id}/invalid_format

In the example above if you call an endpoint `/api/v1/item/{id}`
then the route with alt = `invalid_format` would match.

> You may also try enabling **Random Alt** in **Settings** or dare **Go Quantum**. 😄

# Request variables

To customize a response you can use variables.

Upon each request - variables are collected from multiple sources into a `map`.

They are put into a `map` in the following order:

1. **Bearer JWT** in `Authorization` header - all fields of token payload.
2. **Request payload**.
For REST routes - all fields of the JSON.
For SOAP routes - all fields of the SOAP envelope `Body` tag (without namespace).
Both JSON and SOAP are "flattened" preserving hierarchy. See examples.
3. **Path variables** (`/api/v1/account/{id}`).
4. **Request parameters** (`/api/v1/account?id=1`).
5. **Mock-Variable** header (see section below).

> Note. Variables passed via **Mock-Variable** header
> (which is #5 on the list) will be written into the Map the last
> replacing any variables with the same name that are already in the `map`
> thus having the highest precedence.  


Example of a request payload (JSON body):

    {
        "key1": "value 1",
        "key2": {
            "key1": "other value 1"
        }
    }

The following variables would be available:

    key1 = value 1
    key2.key1 = other value 1

They could be used in a response body like this:

    {
        "response_field1": ${key1},
        "response_field2": ${key2.key1:default_value}
    }

Example of SOAP payload (body):

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
            xmlns:web="http://www.dataaccess.com/webservicesserver/">
        <soapenv:Header/>
        <soapenv:Body>
            <web:NumberToDollarsRequest>
                <web:Value>$74,383.56</web:Value>
            </web:NumberToDollarsRequest>
        </soapenv:Body>
    </soapenv:Envelope>

The following variables would be available:

    ${NumberToDollarsRequest.Value}

# Predefined variables

You can use predefined variables in a response, those would be substituted
with their values each time an endpoint is fetched:

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

# "Mock-Variable" header

Multiple **Mock-Variable** headers supported per HTTP request.
Each header should define exactly one variable.

Header format:

    request-mapping/variable_name/value
    
Example:

    Mock-Variable: api-v1-item-{id}/item_name/Chips

In the example above if you call an endpoint `/api/v1/item/{id}`
a variable `item_name` with the value `Chips` would be available.

# Request Validation

You can define **Request Body Schema** (in JSON format) for any route
to have request body validated against the schema.

> See JSON schema homepage
https://json-schema.org/specification.html

Request body schema may also automatically come from OpenAPI 3 file
for imported routes.
Presence of schema would be indicated at import page.

If validation fails you would receive:
- a route marked with Alt = '400' (this is the default option in Settings).
  A special variable with error message would be available
  to use in response (see Settings).
- or error description (with `type` and `message` fields)
