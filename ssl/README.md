## Creating SSL certificates

Create your own certificate and key

    openssl genrsa -out key.pem 2048
    openssl req -new -key key.pem -out csr.pem
    openssl x509 -req -in csr.pem -signkey key.pem -out cert.pem

Install Flask and Flask-SSLify

    pip install flask flask-sslify

Run app

    python app.py

##Setting up SSL certificates for Java

Verify that cert.pem and key.pem files are valid

    openssl x509 -noout -modulus -in cert.pem | openssl md5
    openssl rsa -noout -modulus -in key.pem | openssl md5

Convert PEM to PKCS #12 for Java

    openssl pkcs12 -export -in cert.pem -inkey key.pem -out cert.p12

(or on Windows)

    openssl pkcs12 -export -in cert.pem -inkey key.pem -out cert.p12 -password pass:YOUR_PASSWORD

Use `cert.p12`.
