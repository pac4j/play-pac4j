<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-play.png" width="300" />
</p>

The `play-pac4j` project is an **easy and powerful security library for Play framework v2** web applications which supports authentication and authorization, but also application logout and advanced features like CSRF protection. It's available under the Apache 2 license and based on the **[pac4j security engine](https://github.com/pac4j/pac4j)**.

Several versions of the library are available for the different versions of the Play framework and for the different languages:

| Play framework | Java library             | Scala library
|----------------|--------------------------|-----------------------------
| Play 2.0       | [play-pac4j_java v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x)   | [play-pac4j_scala2.9 v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x)
| Play 2.1       | [play-pac4j_java v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x)   | [play-pac4j_scala2.10 v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x)
| Play 2.2       | [play-pac4j_java v1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x)   | [play-pac4j_scala v1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x)
| Play 2.3       | [play-pac4j_java v1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x)   | [play-pac4j_scala2.10](https://github.com/pac4j/play-pac4j/tree/1.4.x) and [play-pac4j_scala2.11 v1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x)
| Play 2.4       | play-pac4j-java v2.0.x   | play-pac4j-scala_2.11 v2.0.x

It supports most mechanisms for:
- [**authentication**](https://github.com/pac4j/pac4j/wiki/Clients): CAS, OAuth, SAML, OpenID Connect, LDAP, RDBMS, etc.
- [**authorization**](https://github.com/pac4j/pac4j/wiki/Authorizers): role / permission checks, IP check, CSRF, ...

**Main concepts:**

* A [**client**](https://github.com/pac4j/pac4j/wiki/Clients) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for UI authentication while a direct client is for web services
* An [**authorizer**](https://github.com/pac4j/pac4j/wiki/Authorizers) is meant to check authorizations on the authenticated user profile or on the current web context
* The  `RequiresAuthentication` annotation or function protects an url by checking that:<ul><li>the user is authenticated or starts / performs the login process (according to the clients configuration)</li><li>the authorizations are valid (according to the authorizers configuration)</li></ul>
* The `CallbackController` finishes the authentication process for an indirect client


## How to use Play-Pac4j?

Using `play-pac4j` is very straightforward. First you need to configure your application:

 - First, you need to add a dependency on this library as well as on the appropriate `pac4j` submodules. Then, you must define the [**clients**](https://github.com/pac4j/pac4j/wiki/Clients) for authentication and the [**authorizers**](https://github.com/pac4j/pac4j/wiki/Authorizers) to check authorizations.

 - Define the `CallbackController` to finish authentication processes if you use indirect clients (like Facebook).

Then you are ready to secure (part of) your application:

 - Use the `RequiresAuthentication` annotation (in Java) or function (in Scala) to secure the urls of your web application (using the `clientName` parameter for authentication and the `authorizerName` parameter for authorizations).

 - Alternatively, you can protect multiple URLs by using the `SecurityFilter`.

Just follow these easy steps:

## Configuring your application

Before you can protect your application, you need to add the necessary libraries and configure your application to use `play-pac4j`.

### Add the required dependencies (`play-pac4j` + `pac4j-*` libraries)

You need to add a dependency on the:

- `play-pac4j` library (<em>groupId</em>: **org.pac4j**, *version*: **2.1.0**)

as well as on the appropriate `pac4j` submodules (<em>groupId</em>: **org.pac4j**, *version*: **1.8.3**): the `pac4j-oauth` dependency for OAuth support, the `pac4j-cas` dependency for CAS support, the `pac4j-ldap` module for LDAP authentication, ...

All released artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).


### Define the configuration (`Config` + `Clients` + `XXXClient` + `Authorizer`)

Each authentication mechanism (Facebook, Twitter, a CAS server...) is defined by a client (implementing the `org.pac4j.core.client.Client` interface). All clients must be gathered in a `org.pac4j.core.client.Clients` class.

All `Clients` must be defined in a `org.pac4j.core.config.Config` object as well as the authorizers which will be used by the application. The `Config` is bound for injection in a `SecurityModule` (or whatever the name you call it).

#### In Java:

    public class SecurityModule extends AbstractModule {
    
        ...
        
        @Override
        protected void configure() {
            FacebookClient facebookClient = new FacebookClient("fbId", "fbSecret");
            TwitterClient twitterClient = new TwitterClient("twId", "twSecret");

            FormClient formClient = new FormClient(baseUrl + "/theForm", new SimpleTestUsernamePasswordAuthenticator());
            IndirectBasicAuthClient basicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());
    
            CasClient casClient = new CasClient();
            casClient.setCasLoginUrl("http://mycasserver/login");
    
            SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks",
                    "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:openidp-feide.xml");
            cfg.setMaximumAuthenticationLifetime(3600);
            cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org");
            cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath());
            final SAML2Client saml2Client = new SAML2Client(cfg);
    
            OidcClient oidcClient = new OidcClient();
            oidcClient.setClientID("id");
            oidcClient.setSecret("secret");
            oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
            oidcClient.addCustomParam("prompt", "consent");
    
            ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator("salt"));
    
            Clients clients = new Clients("http://localhost:8080/callback", facebookClient, twitterClient, formClient,
                    basicAuthClient, casClient, saml2Client, oidcClient, parameterClient);
    
            Config config = new Config(clients);
            config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
            config.addAuthorizer("custom", new CustomAuthorizer());
            config.setHttpActionAdapter(new DemoHttpActionAdapter());
            bind(Config.class).toInstance(config);
        }
    }

#### In Scala:

    class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {
    
      override def configure(): Unit = {
    
        val facebookClient = new FacebookClient("fbId", "fbSecret")
        val twitterClient = new TwitterClient("twId", "twSecret")

        val formClient = new FormClient(baseUrl + "/theForm", new SimpleTestUsernamePasswordAuthenticator())
        val basicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator())
    
        val casClient = new CasClient()
        casClient.setCasLoginUrl("http://mycasserver/login")
    
        val cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks", "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:openidp-feide.xml")
        cfg.setMaximumAuthenticationLifetime(3600)
        cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org")
        cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath())
        val saml2Client = new SAML2Client(cfg)
    
        val oidcClient = new OidcClient()
        oidcClient.setClientID("id")
        oidcClient.setSecret("secret")
        oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration")
        oidcClient.addCustomParam("prompt", "consent")
    
        val parameterClient = new ParameterClient("token", new JwtAuthenticator("salt"))
    
        val clients = new Clients("http://localhost:8080/callback", facebookClient, twitterClient, formClient,
          basicAuthClient, casClient, saml2Client, oidcClient, parameterClient)
    
        val config = new Config(clients)
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"))
        config.addAuthorizer("custom", new CustomAuthorizer())
        config.setHttpActionAdapter(new DemoHttpActionAdapter())
        bind(classOf[Config]).toInstance(config)
      }
    }

"http://localhost:8080/callback" is the url of the callback endpoint (see below). It may not be defined for REST support / direct clients only.

Notice the `config.setHttpActionAdapter` call to define the way to handle specific HTTP actions (like redirections, forbidden / unauthorized pages). The only available implementation is currently the `DefaultHttpActionAdapter`, but you can subclass it to define your own HTTP 401 / 403 error pages for example.


### Define the callback endpoint (only for stateful / indirect authentication mechanisms)

Indirect clients rely on external identity providers (like Facebook) and thus require to define a callback endpoint in the application where the user will be redirected after login at the identity provider. For REST support / direct clients only, this callback endpoint is not necessary.  
It must be defined in the `routes` file:

    GET    /callback   org.pac4j.play.CallbackController.callback()

You can also configure it by defining an instance in the `SecurityModule`.

#### In Java:

    CallbackController callbackController = new CallbackController();
    callbackController.setDefaultUrl("/");
    bind(CallbackController.class).toInstance(callbackController);

#### In Scala:

    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/")
    bind(classOf[CallbackController]).toInstance(callbackController)

And using it in the `routes` file:

    GET    /callback   @org.pac4j.play.CallbackController.callback()

The `defaultUrl` parameter defines where the user will be redirected after login if no url was originally requested.

## Protect your application

At this moment `play-pac4j` allows you to secure your application in two ways:

1. Configure security per Action.
2. Use the `SecurityFilter`.

### Protect an url (authentication + authorization) per Action

You can protect an url and require the user to be authenticated by a client (and optionally have the appropriate authorizations) by using the `RequiresAuthentication` annotation or function.

#### In Java:

    @RequiresAuthentication(clientName = "FacebookClient")
    public Result facebookIndex() {
        return protectedIndex();
    }

The following parameters can be defined:

- `clientName` (optional): the list of client names (separated by commas) used for authentication. If the user is not authenticated, direct clients are tried successively then if the user is still not authenticated and if the first client is an indirect one, this client is used to start the authentication. Otherwise, a 401 HTTP error is returned. If the *client_name* request parameter is provided, only the matching client is selected
- `authorizerName` (optional): the list of authorizer names (separated by commas) used to check authorizations. If the user is not authorized, a 403 HTTP error is returned. By default (if blank), the user only requires to be authenticated to access the resource. The following authorizers are available by default:
  * `hsts` to use the `StrictTransportSecurityHeader` authorizer, `nosniff` for `XContentTypeOptionsHeader`, `noframe` for `XFrameOptionsHeader `, `xssprotection` for `XSSProtectionHeader `, `nocache` for `CacheControlHeader ` or `securityHeaders` for the five previous authorizers
  * `csrfToken` to use the `CsrfTokenGeneratorAuthorizer` with the `DefaultCsrfTokenGenerator` (it generates a CSRF token and adds it to the request and save it in the `pac4jCsrfToken` cookie), `csrfCheck` to check that this previous token has been sent as the `pac4jCsrfToken` header or parameter in a POST request and `csrf` to use both previous authorizers.


#### In Scala:

    def facebookIndex = RequiresAuthentication("FacebookClient") { profile =>
      Action { request =>
        Ok(views.html.protectedIndex(profile))
      }
    }

This function is available by using the `org.pac4j.play.scala.Security` trait. You must notice that the user profile is returned along the `RequiresAuthentication` function.

The following functions are available:

- `RequiresAuthentication[A]`
- `RequiresAuthentication[A](clientName: String)`
- `RequiresAuthentication[A](clientName: String, authorizerName: String)`
- `RequiresAuthentication[A](parser: BodyParser[A], clientName: String, authorizerName: String)`

### Protect urls via the SecurityFilter

In order to protect multiple urls at the same tine, you can configure the `SecurityFilter`. You need to configure your application to include the `SecurityFilter` as follows:

First define a `Filters` class in your application (if you have not yet done so).

In Java:

    package filters;

    import org.pac4j.play.filters.SecurityFilter;
    import play.http.HttpFilters;
    import play.api.mvc.EssentialFilter;

    import javax.inject.Inject;

    public class Filters implements HttpFilters {

        private final SecurityFilter securityFilter;

        @Inject
        public Filters(SecurityFilter securityFilter) {
            this.securityFilter = securityFilter;
        }

        @Override
        public EssentialFilter[] filters() {
            return new EssentialFilter[] {securityFilter};
        }
    }

In Scals:

    package filters

    import javax.inject.Inject
    import org.pac4j.play.filters.SecurityFilter
    import play.api.http.HttpFilters

    /**
     * Configuration of all the Play filters that are used in the application.
     */
    class Filters @Inject()(securityFilter: SecurityFilter) extends HttpFilters {

      def filters = Seq(securityFilter)

    }

Then tell your application to use the filters in `application.conf`:

    play.http.filters = "filters.Filters"

See for more information on the use of filters in Play the [Play documentation on Filters](https://www.playframework.com/documentation/2.4.x/ScalaHttpFilters).

Rules for the security filter can be supplied in application.conf. An example is shown below. It
consists of a list of filter rules, where the key is a regular expression that will be used to
match the url. Make sure that the / is escaped by \\ to make a valid regular expression.

For each regex key, there are two subkeys: `authorizers` and `clients`. Here you can define the
correct values, like you would supply to the `RequireAuthentication` method in controllers. There
two exceptions: `authorizers` can have two special values: `_authenticated_` and `_anonymous_`.

`_anonymous_` will disable authentication and authorization for urls matching the regex.
`_authenticated_` will require authentication, but will set clients and authorizers both to `null`.

Rules are applied top to bottom. The first matching rule will define which clients and authorizers
are used. When not provided, the value will be `null`.

    pac4j.security.rules = [
      # Admin pages need a special authorizer and do not support login via Twitter.
      {"/admin/.*" = {
        authorizers = "admin"
        clients = "FormClient"
      }}
      # Rules for the REST services. These don't specify a client and will return 401
      # when not authenticated.
      {"/restservices/.*" = {
        authorizers = "_authenticated_"
      }}
      # The login page needs to be publicly accessible.
      {"/login.html" = {
        authorizers = "_anonymous_"
      }}
      # 'Catch all' rule to make sure the whole application stays secure.
      {".*" = {
        authorizers = "_authenticated_"
        clients = "FormClient,TwitterClient"
      }}
    ]

### Get the user profile

#### In Java:
 
You need to inherit from the `UserProfileController` and call the `getUserProfile()` method.
 
#### In Scala:

You need to extend from the `Security` trait and call the `getUserProfile(request: RequestHeader)` function. 

You can also directly use the `ProfileManager.get(true)` method (`false` not to use the session, but only the current HTTP request) and the `ProfileManager.isAuthenticated()` method. 

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common properties that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:
 
    FacebookProfile facebookProfile = (FacebookProfile) commonProfile;


### Logout

You can log out the current authenticated user using the `ApplicationLogoutController` defined in the `routes` file:

    GET  /logout  org.pac4j.play.ApplicationLogoutController.logout()

To perfom the logout, you must call the /logout url. A blank page is displayed by default unless an *url* request parameter is provided. In that case, the user will be redirected to this specified url (if it matches the logout url pattern defined) or to the default logout url otherwise.

You can configure this controller by defining an instance in the `SecurityModule`.

#### In Java:

    ApplicationLogoutController logoutController = new ApplicationLogoutController();
    logoutController.setDefaultUrl("/");
    bind(ApplicationLogoutController.class).toInstance(logoutController);

#### In Scala:

    val logoutController = new ApplicationLogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[ApplicationLogoutController]).toInstance(logoutController)

And using it in the `routes` file:

    GET  /logout  @org.pac4j.play.ApplicationLogoutController.logout()

The following parameters can be defined:

- `defaultUrl` (optional): the default logout url if the provided *url* parameter does not match the `logoutUrlPattern` (by default: /)
- `logoutUrlPattern` (optional): the logout url pattern that the logout url must match (it's a security check, only relative urls are allowed by default).

## Migration guide

### 2.0.1 -> 2.1.0
The separate Scala and Java projects have been merged. You need to change the dependency `play-pac4j-java` or `play-pac4j-scala` to simply `play-pac4j`.

The `Security` trait for use in Scala project has moved to package `org.pac4j.play`.

### 2.0.0 -> 2.0.1

The `DataStore` concept is replaced by the pac4j `SessionStore` concept. The `PlayCacheStore` does no longer need to be bound in the security module. A new session store could be defined using the `config.setSessionStore` method.

The `DefaultHttpActionAdapter` does not need to be bound in the security module, but must to be set using the `config.setHttpActionAdapter` method.


### 1.5.x -> 2.0.0

`play-pac4j v2.0` is a huge refactoring of the previous version 1.5. It takes advantage of the new features of `pac4j` v1.8 (REST support, authorizations, configuration objects...) and is fully based on dependency injection -> see [Play 2.4 migration guide](https://www.playframework.com/documentation/2.4.x/Migration24).

In Java, the `SecurityController` and `JavaController` are deprecated and you need to use the `UserProfileController` to get the user profile (you can also use the `ProfileManager` object directly).

The "target url" concept has disappeared as it was too complicated, it could be simulated though.

The `SecurityCallbackController` is deprecated and you must use the `CallbackController`. The logout support has been moved to the `ApplicationLogoutController`.

The `JavaWebContext` and `ScalaWebContext` have been merged into a new `PlayWebContext`.

The `StorageHelper` has been removed, replaced by the `PlayCacheStore` implementation where you can set the timeouts. You can provide your own implementation of the `CacheStore` if necessary.

The `PlayLogoutHandler` has been moved to the `org.pac4j.play.cas.logout` package and renamed as `PlayCacheLogoutHandler` (it relies on the Play Cache).

The static specific `Config` has been replaced by the default `org.pac4j.core.config.Config` object to define the clients (authentication) and the authorizers (authorizations).

Custom 401 / 403 HTTP error pages must now be defined by overriding the `DefaultHttpActionAdapter`.

The `isAjax` parameter is no longer available as AJAX requests are now automatically detected. The `stateless` parameter is no longer available as the stateless nature is held by the client itself.
The `requireAnyRole` and `requieAllRoles` parameters are no longer available and authorizers must be used instead (with the `authorizerName` parameter).


## Demo

Two demo webapps: [play-pac4j-java-demo](https://github.com/pac4j/play-pac4j-java-demo) & [play-pac4j-scala-demo](https://github.com/pac4j/play-pac4j-scala-demo) are available for tests and implement many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...

Test them online: [http://play-pac4j-java-demo.herokuapp.com](http://play-pac4j-java-demo.herokuapp.com) and [http://play-pac4j-scala-demo.herokuapp.com](http://play-pac4j-scala-demo.herokuapp.com).

## Release notes

See the [release notes](https://github.com/pac4j/play-pac4j/wiki/Release-notes). Learn more by browsing the [play-pac4j Javadoc](http://www.pac4j.org/apidocs/play-pac4j/2.0.1/index.html) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/1.8.3/index.html).


## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)


## Development

The version 2.0.2-SNAPSHOT is under development.

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/play-pac4j.png?branch=master)](https://travis-ci.org/pac4j/play-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the `resolvers` of your `build.sbt` file:

    resolvers ++= Seq( Resolver.mavenLocal,
        "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
        "Pablo repo" at "https://raw.github.com/fernandezpablo85/scribe-java/mvn-repo/")
