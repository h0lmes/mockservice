from flask import Flask, jsonify
from flask_sslify import SSLify

# Create Flask app
app = Flask(__name__)

# Apply SSLify to enforce SSL on all routes
sslify = SSLify(app)

@app.route('/hello', methods=['GET'])
def hello():
    return jsonify(message="Hello, this is a secure endpoint!")

if __name__ == '__main__':
    # Run the app with SSL
    # Make sure to provide the correct paths to your SSL cert and key
    app.run(ssl_context=('cert.pem', 'key.pem'), host='0.0.0.0', port=443)
