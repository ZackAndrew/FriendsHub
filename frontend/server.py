import http.server
import urllib.request
import urllib.error
import socket
import mimetypes
import sys
import os

PORT = 5173
BACKEND_URL = os.environ.get("BACKEND_URL", "http://localhost:8060")
DIRECTORY = os.path.dirname(os.path.abspath(__file__))

# Explicitly ensure correct web MIME types
mimetypes.init()
mimetypes.add_type("application/javascript", ".js")
mimetypes.add_type("text/css", ".css")
mimetypes.add_type("text/html", ".html")
mimetypes.add_type("image/svg+xml", ".svg")
mimetypes.add_type("image/x-icon", ".ico")

class ProxyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()

    def do_GET(self):
        # Empty 200 response for favicon if not found
        if self.path == '/favicon.ico':
            self.send_response(204)
            self.end_headers()
            return

        if self.path.startswith('/api/'):
            self.proxy_request()
        else:
            # Fallback to index.html for SPA if not a file
            local_path = os.path.join(DIRECTORY, self.path.lstrip('/').split('?')[0])
            if not os.path.exists(local_path) and not '.' in os.path.basename(self.path):
                self.path = '/index.html'
            super().do_GET()

    def do_POST(self):
        if self.path.startswith('/api/'):
            self.proxy_request()
        else:
            self.send_error(405, "Method not allowed")

    def do_PUT(self):
        if self.path.startswith('/api/'):
            self.proxy_request()
        else:
            self.send_error(405, "Method not allowed")

    def do_PATCH(self):
        if self.path.startswith('/api/'):
            self.proxy_request()
        else:
            self.send_error(405, "Method not allowed")

    def do_DELETE(self):
        if self.path.startswith('/api/'):
            self.proxy_request()
        else:
            self.send_error(405, "Method not allowed")

    def proxy_request(self):
        target_url = f"{BACKEND_URL}{self.path}"
        content_length = int(self.headers.get('Content-Length', 0))
        post_data = self.rfile.read(content_length) if content_length > 0 else None

        req = urllib.request.Request(target_url, data=post_data, method=self.command)
        for key, val in self.headers.items():
            if key.lower() not in ['host', 'content-length']:
                req.add_header(key, val)

        try:
            with urllib.request.urlopen(req, timeout=5) as response:
                self.send_response(response.status)
                for key, val in response.getheaders():
                    if key.lower() not in ['transfer-encoding', 'content-length']:
                        self.send_header(key, val)
                body = response.read()
                self.send_header('Content-Length', str(len(body)))
                self.end_headers()
                self.wfile.write(body)
        except urllib.error.HTTPError as e:
            self.send_response(e.code)
            for key, val in e.headers.items():
                if key.lower() not in ['transfer-encoding', 'content-length']:
                    self.send_header(key, val)
            body = e.read()
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        except Exception as e:
            # Backend not reachable -> return 503 so frontend can fallback to demo mode
            self.send_response(503)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(b'{"error": "Backend service unavailable", "status": 503}')

class DualStackServer(http.server.ThreadingHTTPServer):
    def __init__(self, server_address, RequestHandlerClass):
        if socket.has_dualstack_ipv6():
            self.address_family = socket.AF_INET6
        super().__init__(server_address, RequestHandlerClass)

    def server_bind(self):
        if self.address_family == socket.AF_INET6:
            try:
                self.socket.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_V6ONLY, 0)
            except Exception:
                pass
        super().server_bind()

if __name__ == '__main__':
    bind_addr = "" if socket.has_dualstack_ipv6() else "0.0.0.0"
    with DualStackServer((bind_addr, PORT), ProxyHTTPRequestHandler) as httpd:
        print(f"FriendsHub Frontend running at http://localhost:{PORT} and http://127.0.0.1:{PORT}")
        sys.stdout.flush()
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            pass
