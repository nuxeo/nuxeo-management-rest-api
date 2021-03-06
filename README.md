# Nuxeo Management REST API

Set of REST endpoints allowing to manage the Nuxeo Platform.

## Management REST API Authentication

The Management REST API is accessible for any administrator user.

Furthermore, a "technical" user can be configured to access the Management REST API in _nuxeo.conf_:

```
org.nuxeo.rest.management.user=transient/technical_user
```

The user does not need to exist in Nuxeo, and **must** start with `transient/` as we are relying on the transient user feature.

First, configure a JWT secret in _nuxeo.conf_:

```
nuxeo.jwt.secret=abracadabra
```

Then, to use the Mangement REST API:

- share the JWT secret (`abracadabra` here) between the Nuxeo Server and the client calling the Management REST API
- generate a JWT token with the user (`transient/technical_user` here) as claim subject
- call the API using the `Authorization: Bearer JWT_TOKEN` header

## Deploy Management REST API on separate port

Management REST API could be deployed on a separate port than the regular Nuxeo application.

You need to configure several things to make Management REST API running on a different port:

- Configure _nuxeo.conf_
- Add a _Connector_ in Tomcat configuration

### Configure _nuxeo.conf_

You need to configure the Management REST API HTTP port:

```
nuxeo.server.http.managementPort=9090
```

With this, API will only be accessible on port `9090`.

### Configure _Connector_

In order to add a new `Connector`, you need to add a new Tomcat _Connector_ for the Catalina _Service_ in `./conf/server.xml` like the one below:

```xml
<Connector port="9090" protocol="HTTP/1.1" URIEncoding="UTF-8"
           address="0.0.0.0"
           maxThreads="2"
           acceptCount="10"
           compression="on"
           compressionMinSize="512"
           compressibleMimeType="text/css,application/javascript,text/xml,text/html"
           connectionTimeout="20000"
           disableUploadTimeout="false"
           connectionUploadTimeout="60000" />
```

Here below the Freemarker version using nuxeo.conf properties:

```xml
<Connector port="${nuxeo.server.http.managementPort}" protocol="HTTP/1.1" URIEncoding="UTF-8"
               address="${nuxeo.bind.address}"
<#if nuxeo.server.signature??>
               server="${nuxeo.server.signature}"
</#if>
               maxThreads="2"
               acceptCount="10"
               compression="on"
               compressionMinSize="512"
               compressibleMimeType="text/css,application/javascript,text/xml,text/html"
               connectionTimeout="20000"
               disableUploadTimeout="false"
               connectionUploadTimeout="${nuxeo.server.http.connectionUploadTimeout}" />
```

You can find the parent template holding these configurations under `./templates/common-base/conf/server.xml.nxftl`
