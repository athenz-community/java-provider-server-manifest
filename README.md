Run the server locally:

```sh
git clone git@github.com:athenz-community/java-provider-server-manifest.git oss_sample_java_api_server
make -C oss_sample_java_api_server local
```

Then, open a new terminal and run:

```sh
curl localhost:14443/api/docs

# {"docs":[{"name":"first doc","id":1,"content":"hello world"}]}
```

