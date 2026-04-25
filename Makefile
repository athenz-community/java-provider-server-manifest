PORT ?= 14443
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

