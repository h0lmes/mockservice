Create your own certificate and key

    openssl genrsa -out key.pem 2048
    openssl req -new -key key.pem -out csr.pem
    openssl x509 -req -in csr.pem -signkey key.pem -out cert.pem

Install Flask and Flask-SSLify

    pip install flask flask-sslify

Run app

    python app.py

