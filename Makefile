PORT ?= 14443
PORT_MCP ?= 8101
AT_REQUIRED ?= true

local:
	@echo "Running Java Server... [PORT=$(PORT), AT_REQUIRED=$(AT_REQUIRED)]"
	PORT=$(PORT) AT_REQUIRED=$(AT_REQUIRED) mvn clean compile exec:java \
		-Dmaven.resolver.transport=wagon \
		-Dmaven.wagon.http.ssl.insecure=true \
		-Dmaven.wagon.http.ssl.allowall=true

update-policy:
	@curl -s -k -X GET "https://localhost:8443/zts/v1/domain/api/signed_policy_data" \
  -H "Accept: application/json" \
  --cert ./athenz_dist/certs/athenz_admin.cert.pem \
  --key ./athenz_dist/keys/athenz_admin.private.pem > api.pol

mcp-local:
	@if [ ! -d "mcp/node_modules" ]; then \
		echo "Installing npm dependencies in mcp/ folder..."; \
		(cd mcp && npm install); \
	fi
	@if [ ! -d "venv" ]; then \
		echo "Creating venv and installing mcpo..."; \
		python3 -m venv venv && ./venv/bin/pip install mcpo; \
	fi
	@echo "🚀 Running MCP Server on port $(PORT_MCP)..."
	@./venv/bin/mcpo --port $(PORT_MCP) -- node mcp/server.js