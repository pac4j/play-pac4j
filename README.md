<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-play.png" width="300" />
</p>

The `play-pac4j` project is an **easy and powerful security library for Play framework v2 web applications and web services** which supports authentication and authorization, but also logout and advanced features like CSRF protection. It can work with Deadbolt.
It's based on JDK 11 and Play 2.8 (Scala v2.12 or v2.13) and on the **[pac4j security engine](https://github.com/pac4j/pac4j) v5**. It's available under the Apache 2 license.

Several versions of the library are available depending on the version of the Play framework:

| Play version | pac4j version | play-pac4j version
|--------------|---------------|-------------------
| 2.0          | 1.7           | [play-pac4j_java v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Java) / [play-pac4j_scala2.9 v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Scala)
| 2.1          | 1.7           | [play-pac4j_java v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Java) / [play-pac4j_scala2.10 v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Scala)
| 2.2          | 1.7           | [play-pac4j_java v1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x) (Java) / [play-pac4j_scala v1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x) (Scala)
| 2.3          | 1.7           | [play-pac4j_java v1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x) (Java) / [play-pac4j_scala2.10 v1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x) and [play-pac4j_scala2.11 v1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x) (Scala)
| 2.4          | 1.9           | [play-pac4j v2.3.x](https://github.com/pac4j/play-pac4j/tree/2.3.x) (Java & Scala)
| 2.5          | 2.x           | [play-pac4j_2.11 and play-pac4j_2.12 v3.1.x](https://github.com/pac4j/play-pac4j/tree/3.1.x) (Java & Scala)
| 2.6          | 3.x           | [play-pac4j_2.11 and play-pac4j_2.12 v7.0.x](https://github.com/pac4j/play-pac4j/tree/7.0.x) (Java & Scala) **Do NOT use Play v2.6.3 and v2.6.5 which have issues in their Cache implementations!**
| 2.7          | 3.x           | [play-pac4j_2.11 and play-pac4j_2.12 v8.0.x](https://github.com/pac4j/play-pac4j/tree/8.0.x) (Java & Scala)
| 2.7          | 4.x           | [play-pac4j_2.11 and play-pac4j_2.12 and play-pac4j_2.13 v9.0.x](https://github.com/pac4j/play-pac4j/tree/9.0.x) (Java & Scala)
| 2.8          | 4.x           | [play-pac4j_2.12 and play-pac4j_2.13 v9.0.x](https://github.com/pac4j/play-pac4j/tree/10.0.x) (Java & Scala)
| 2.8          | 5.x           | play-pac4j_2.12 and play-pac4j_2.13 v11.0.x-PLAY2.8 (Java & Scala)


[**Main concepts and components:**](http://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](http://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for UI authentication while a direct client is for web services authentication:

&#9656; OAuth - SAML - CAS - OpenID Connect - HTTP - Google App Engine - Kerberos - LDAP - SQL - JWT - MongoDB - CouchDB - IP address - REST API

2) An [**authorizer**](http://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles / permissions - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) A [**matcher**](http://www.pac4j.org/docs/matchers.html) defines whether the security must be applied and can be used for additional web processing

4) The `Secure` annotation and the `Security` trait protect methods while the `SecurityFilter` protects URLs by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

5) The `CallbackController` finishes the login process for an indirect client

6) The `LogoutController` logs out the user from the application and triggers the logout at the identity provider level

7) The `Pac4jScalaTemplateHelper` can be used to get the user profile(s) from a Twirl template.


## Usage

### 1) [Add the required dependencies](https://github.com/pac4j/play-pac4j/wiki/Dependencies)

### 2) Define:

### - the [security configuration](https://github.com/pac4j/play-pac4j/wiki/Security-configuration)
### - the [callback configuration](https://github.com/pac4j/play-pac4j/wiki/Callback-configuration), only for web applications
### - the [logout configuration](https://github.com/pac4j/play-pac4j/wiki/Logout-configuration)

### 3) [Apply security](https://github.com/pac4j/play-pac4j/wiki/Apply-security)

### 4) [Get the authenticated user profiles](https://github.com/pac4j/play-pac4j/wiki/Get-the-authenticated-user-profiles)


## Demos

Two demo webapps: [play-pac4j-java-demo](https://github.com/pac4j/play-pac4j-java-demo) & [play-pac4j-scala-demo](https://github.com/pac4j/play-pac4j-scala-demo) are available for tests and implement many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...

Test them online: [http://play-pac4j-java-demo.herokuapp.com](http://play-pac4j-java-demo.herokuapp.com) and [http://play-pac4j-scala-demo.herokuapp.com](http://play-pac4j-scala-demo.herokuapp.com).


## Versions

The latest released version is the [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.pac4j/play-pac4j-parent/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.pac4j/play-pac4j-parent), available in the [Maven central repository](https://repo.maven.apache.org/maven2).
The [next version](https://github.com/pac4j/play-pac4j/wiki/Next-version) is under development.

See the [release notes](https://github.com/pac4j/play-pac4j/wiki/Release-notes). Learn more by browsing the [pac4j documentation](https://www.javadoc.io/doc/org.pac4j/pac4j-core/4.0.1/index.html) and the [play-pac4j_2.12 Javadoc](http://www.javadoc.io/doc/org.pac4j/play-pac4j_2.12/10.0.1) / [play-pac4j_2.13 Javadoc](http://www.javadoc.io/doc/org.pac4j/play-pac4j_2.13/10.0.1).

See the [migration guide](https://github.com/pac4j/play-pac4j/wiki/Migration-guide) as well.


## Need help?

You can use the [mailing lists](http://www.pac4j.org/mailing-lists.html) or the [commercial support](http://www.pac4j.org/commercial-support.html).
