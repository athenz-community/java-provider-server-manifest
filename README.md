

```
mvn clean compile -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true
```


```
mvn compile -Dmaven.resolver.transport=wagon -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true
```

```sh
mvn compile \
  -Dmaven.resolver.transport=wagon \
  -Dmaven.wagon.http.ssl.insecure=true \
  -Dmaven.wagon.http.ssl.allowall=true \
  -Dmaven.wagon.http.ssl.ignore.validity.dates=true
```

Then:

```sh
mvn compile
```

Then:

```sh
mvn exec:java \
  -U \
  -Dmaven.resolver.transport=wagon \
  -Dmaven.wagon.http.ssl.insecure=true \
  -Dmaven.wagon.http.ssl.allowall=true \
  -Dmaven.wagon.http.ssl.ignore.validity.dates=true
```

```sh
mvn exec:java
```

Then:

```sh
curl localhost:14443/api/docs

# {"docs":[{"name":"first doc","id":1,"content":"hello world"}]}
```