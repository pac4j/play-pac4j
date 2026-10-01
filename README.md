<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-play.png" width="300" />
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/org.pac4j/play-pac4j_3"><img src="https://img.shields.io/maven-central/v/org.pac4j/play-pac4j_3?label=Maven%20Central" alt="Maven Central" /></a>
  <a href="https://github.com/pac4j/play-pac4j/actions/workflows/ci.yml"><img src="https://github.com/pac4j/play-pac4j/actions/workflows/ci.yml/badge.svg" alt="Build status" /></a>
  <img src="https://img.shields.io/badge/Java-17%2B-blue" alt="Java 17+" />
  <img src="https://img.shields.io/badge/Play-3.0-blue" alt="Play 3.0" />
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache%202.0-blue" alt="Apache 2 license" /></a>
</p>

> `play-pac4j` is the Play framework implementation of **[pac4j](https://github.com/pac4j/pac4j)**, the security engine for Java.
> If it is useful to you, please ⭐ **[star pac4j on GitHub](https://github.com/pac4j/pac4j)**: it helps other developers discover it!

The `play-pac4j` project is an **easy and powerful security library for Play Framework web applications and web services** which supports authentication and authorization, but also logout and advanced features like CSRF protection. It can work with Deadbolt.
It's based on the **[pac4j security engine](https://github.com/pac4j/pac4j)**. It's available under the Apache 2 license.

| JDK  | Play version | pac4j version | play-pac4j version | Modules (Java & Scala)                          | Usage of Lombok | Status           |
|------|--------------|---------------|--------------------|-------------------------------------------------|-----------------|------------------|
| 17   | 3.0          | 6.x           | 14.0.0-SNAPSHOT    | play-pac4j_3                                  | Yes             | In development   |
| 17   | 3.0          | 6.x           | 13.0.x-PLAY3.0     | play-pac4j_2.13 play-pac4j_3                    | Yes             | Production ready |
| 17   | 3.0          | 6.x           | 12.0.x-PLAY3.0     | play-pac4j_2.13 play-pac4j_3                    | Yes             | Production ready |
| 17   | 2.9          | 6.x           | 12.0.x-PLAY2.9     | play-pac4j_2.13 play-pac4j_3                    | Yes             | Production ready |
| 17   | 2.8          | 6.x           | 12.0.x-PLAY2.8     | play-pac4j_2.12 play-pac4j_2.13                 | Yes             | Production ready |
| 11   | 2.8          | 5.x           | 11.1.x-PLAY2.8     | play-pac4j_2.12 play-pac4j_2.13                 | No              | Production ready |
| 11   | 2.8          | 5.x           | 11.0.x-PLAY2.8     | play-pac4j_2.12 play-pac4j_2.13                 | No              | Production ready |
| 11   | 2.8          | 4.x           | 10.x               | play-pac4j_2.12 play-pac4j_2.13                 | No              | Production ready |
| 8    | 2.7          | 4.x           | 9.x                | play-pac4j_2.11 play-pac4j_2.12 play-pac4j_2.13 | No              | Production ready |

Version 14 targets Play 3 and Scala 3 only, with both Java and Scala APIs. Java applications must also configure their Play build to use Scala 3.

[**Main concepts and components:**](http://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](http://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for UI authentication while a direct client is for web services authentication:

&#9656; OpenID Connect - SAML - CAS - OAuth - HTTP - Kerberos - LDAP - SQL - JWT - MongoDB - IP address - REST API

2) An [**authorizer**](http://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

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


## Versions

The latest released version is the [![Maven Central](https://img.shields.io/maven-central/v/org.pac4j/play-pac4j_3.svg)](https://repo1.maven.org/maven2/org/pac4j/play-pac4j_3).
The [next version](https://github.com/pac4j/play-pac4j/wiki/Next-version) is under development.

See the [release notes](https://github.com/pac4j/play-pac4j/wiki/Release-notes).

See the [migration guide](https://github.com/pac4j/play-pac4j/wiki/Migration-guide) as well.


## Need help?

You can use the [mailing lists](http://www.pac4j.org/mailing-lists.html) or the [commercial support](http://www.pac4j.org/commercial-support.html).
