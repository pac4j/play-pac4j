<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-play.png" width="300" />
</p>

The `play-pac4j` project is an **easy and powerful security library for Play framework v2 web applications and web services** which supports authentication and authorization, but also logout and advanced features like CSRF protection. It can work with Deadbolt.
It's based on Play 2.7 (and Scala 2.11 or Scala 2.12) and on the **[pac4j security engine](https://github.com/pac4j/pac4j) v3**. It's available under the Apache 2 license.

Several versions of the library are available for the different versions of the Play framework:

| Play version | pac4j version | play-pac4j version
|--------------|---------------|-------------------
| 2.0          | 1.7           | [play-pac4j_java 1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Java) / [play-pac4j_scala2.9 1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Scala)
| 2.1          | 1.7           | [play-pac4j_java 1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Java) / [play-pac4j_scala2.10 1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Scala)
| 2.2          | 1.7           | [play-pac4j_java 1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x) (Java) / [play-pac4j_scala 1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x) (Scala)
| 2.3          | 1.7           | [play-pac4j_java 1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x) (Java) / [play-pac4j_scala2.10](https://github.com/pac4j/play-pac4j/tree/1.4.x) and [play-pac4j_scala2.11 1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x) (Scala)
| 2.4          | 1.9           | [play-pac4j 2.3.x](https://github.com/pac4j/play-pac4j/tree/2.3.x) (Java & Scala)
| 2.5          | 2.x           | [play-pac4j_2.11 and play-pac4j_2.12 3.1.x](https://github.com/pac4j/play-pac4j/tree/3.1.x) (Java & Scala)
| 2.6          | 3.x           | [play-pac4j_2.11 and play-pac4j_2.12 7.0.x](https://github.com/pac4j/play-pac4j/tree/7.0.x) (Java & Scala)
| 2.7          | 3.7           | 8.0.x (Java & Scala)

**Do NOT use Play 2.6.3 and 2.6.5 versions which have issues in their Cache implementations!**

[**Main concepts and components:**](http://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](http://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for UI authentication while a direct client is for web services authentication:

&#9656; OAuth - SAML - CAS - OpenID Connect - HTTP - OpenID - Google App Engine - Kerberos - LDAP - SQL - JWT - MongoDB - CouchDB - IP address - REST API

2) An [**authorizer**](http://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles / permissions - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) The `Secure` annotation and the `Security` trait protect methods while the `SecurityFilter` protects URLs by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

4) The `CallbackController` finishes the login process for an indirect client

5) The `LogoutController` logs out the user from the application and triggers the logout at the identity provider level

6) The `Pac4jScalaTemplateHelper` can be used to get the user profile(s) from a Twirl template.


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

See the [release notes](https://github.com/pac4j/play-pac4j/wiki/Release-notes). Learn more by browsing the [pac4j documentation](http://www.pac4j.org/3.7.0/docs/index.html) and the [play-pac4j_2.11 Javadoc](http://www.javadoc.io/doc/org.pac4j/play-pac4j_2.11/8.0.2) / [play-pac4j_2.12 Javadoc](http://www.javadoc.io/doc/org.pac4j/play-pac4j_2.12/8.0.2).

See the [migration guide](https://github.com/pac4j/play-pac4j/wiki/Migration-guide) as well.


## Need help?

If you need commercial support (premium support or new/specific features), contact us at [info@pac4j.org](mailto:info@pac4j.org).

If you have any questions, want to contribute or be notified about the new releases and security fixes, please subscribe to the following [mailing lists](http://www.pac4j.org/mailing-lists.html):

- [pac4j-users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j-developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)
- [pac4j-announce](https://groups.google.com/forum/?hl=en#!forum/pac4j-announce)
- [pac4j-security](https://groups.google.com/forum/#!forum/pac4j-security)
