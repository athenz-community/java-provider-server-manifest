PORT ?= 14443
AT_REQUIRED ?= true

local:
	@echo "Running Java Server... [PORT=$(PORT), AT_REQUIRED=$(AT_REQUIRED)]"
	PORT=$(PORT) AT_REQUIRED=$(AT_REQUIRED) mvn clean compile exec:java \
		-Dmaven.resolver.transport=wagon \
		-Dmaven.wagon.http.ssl.insecure=true \
		-Dmaven.wagon.http.ssl.allowall=true