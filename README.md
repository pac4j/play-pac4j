<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-play.png" width="50%" height="50%" />
</p>

The `play-pac4j` project is an **easy and powerful security library for Play framework v2** web applications which supports authentication and authorization, but also application logout and advanced features like CSRF protection. It's available under the Apache 2 license and based on the [pac4j](https://github.com/pac4j/pac4j) library.

Several versions of the library are available for the different versions of the Play framework and for the different languages:

| Play framework | Java library             | Scala library
|----------------|--------------------------|-----------------------------
| Play 2.0       | [play-pac4j_java v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x)   | [play-pac4j_scala2.9 v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x)
| Play 2.1       | [play-pac4j_java v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x)   | [play-pac4j_scala2.10 v1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x)
| Play 2.2       | [play-pac4j_java v1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x)   | [play-pac4j_scala v1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x)
| Play 2.3       | [play-pac4j_java v1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x)   | [play-pac4j_scala2.10](https://github.com/pac4j/play-pac4j/tree/1.4.x) and [play-pac4j_scala2.11 v1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x)
| Play 2.4       | play-pac4j-java v2.0.x   | play-pac4j-scala_2.11 v2.0.x

It supports most authentication mechanisms, called ["clients"](https://github.com/pac4j/pac4j/wiki/Clients):

- **indirect / stateful clients** are for UI when the user authenticates once at an external provider (like Facebook, a CAS server...) or via a local form (or basic auth popup)  
- **direct / stateless clients** are for web services when credentials (like basic auth, tokens...) are passed for each HTTP request.

See the [authentication flows](https://github.com/pac4j/pac4j/wiki/Authentication-flows).

| The authentication mechanism you want | The `pac4j-*` submodule you must use
|---------------------------------------|-------------------------------------
| OAuth (1.0 & 2.0): Facebook, Twitter, Google, Yahoo, LinkedIn, Github... | `pac4j-oauth`
| CAS (1.0, 2.0, 3.0, SAML, logout, proxy, REST) | `pac4j-cas`
| HTTP (form, basic auth, IP, header, cookie, GET/POST parameter) | `pac4j-http`
| OpenID | `pac4j-openid`
| SAML (2.0) | `pac4j-saml`
| Google App Engine UserService | `pac4j-gae`
| OpenID Connect (1.0) | `pac4j-oidc`
| JWT | `pac4j-jwt`
| LDAP | `pac4j-ldap`
| Relational DB | `pac4j-sql`
| MongoDB | `pac4j-mongo`
| Stormpath | `pac4j-stormpath`

It also supports many authorization checks, called [**authorizers**](https://github.com/pac4j/pac4j/wiki/Authorizers) available in the `pac4j-core` and `pac4j-http` submodules: role / permission checks, CSRF token validation...


## How to use it?

First, you need to add a dependency on this library as well as on the appropriate `pac4j` submodules. Then, you must define the [**clients**](https://github.com/pac4j/pac4j/wiki/Clients) for authentication and the [**authorizers**](https://github.com/pac4j/pac4j/wiki/Authorizers) to check authorizations.

Define the `CallbackController` to finish authentication processes if you use indirect clients (like Facebook).

Use the `RequiresAuthentication` annotation (in Java) or function (in Scala) to secure the urls of your web application (using the `clientName` parameter for authentication and the `authorizerName` parameter for authorizations).

Just follow these easy steps:


### Add the required dependencies (`play-pac4j-java` or `play-pac4j-scala_2.11` + `pac4j-*` libraries)

You need to add a dependency on the:

- `play-pac4j-java` library (<em>groupId</em>: **org.pac4j**, *version*: **2.0.0-SNAPSHOT**) if you code in Java
- `play-pac4j-scala_2.11` library (<em>groupId</em>: **org.pac4j**, *version*: **2.0.0-SNAPSHOT**) if you use Scala

as well as on the appropriate `pac4j` modules (<em>groupId</em>: **org.pac4j**, *version*: **1.8.0-RC1**): the `pac4j-oauth` dependency for OAuth support, the `pac4j-cas` dependency for CAS support, the `pac4j-ldap` module for LDAP authentication, ...


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
        bind(classOf[Config]).toInstance(config)
      }
    }

"http://localhost:8080/callback" is the url of the callback endpoint (see below). It may not be defined for REST support only.


### Define the data store (`PlayCacheStore`)

Some of the data used by `play-pac4j` (user profile, tokens...) must be saved somewhere. Thus, a datastore must be defined in the `SecurityModule`.  
The only existing implementation is currently the `PlayCacheStore` (where all data are saved into the `Cache`). <font color="red">If you have multiple Play nodes, you need a shared `Cache` between all your nodes.</font>

#### In Java:

    bind(DataStore.class).to(PlayCacheStore.class);

#### In Scala:

    bind(classOf[DataStore]).to(classOf[PlayCacheStore])


### Define the HTTP action adapter (`DefaultHttpActionAdapter`)

To handle specific HTTP actions (like redirections, forbidden / unauthorized pages), you need to define the appropriate `HttpActionAdapter`. The only available implementation is currently the `DefaultHttpActionAdapter`, but you can subclass it to define your own HTTP 401 / 403 error pages for example.
Its binding must be defined in the `SecurityModule`.

#### In Java:

    bind(HttpActionAdapter.class).to(DefaultHttpActionAdapter.class);

#### In Scala:

    bind(classOf[HttpActionAdapter]).to(classOf[DefaultHttpActionAdapter])


### Define the callback endpoint (only for stateful / indirect authentication mechanisms)

Some authentication mechanisms rely on external identity providers (like Facebook) and thus require to define a callback endpoint where the user will be redirected after login at the identity provider. For REST support only, this callback endpoint is not necessary.  
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


### Protect an url (authentication + authorization)

You can protect an url and require the user to be authenticated by a client (and optionally have the appropriate authorizations) by using the `RequiresAuthentication` annotation or function.

#### In Java:

    @RequiresAuthentication(clientName = "FacebookClient")
    public Result facebookIndex() {
        return protectedIndex();
    }

The following parameters can be defined:

- `clientName` (optional): the list of client names (separated by commas) used for authentication. If the user is not authenticated, direct clients are tried successively. If the user is still not authenticated and if the first client is an indirect one, it is used to start the authentication. If the *client_name* request parameter is provided, only the matching client is selected
- `authorizerName` (optional): the authorizer name (or a list of authorizer names separated by commas) which will protect the resource (they must exist in the authorizers configuration). By default (if blank), the user only requires to be authenticated to access the resource.

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


### Get redirection urls

You can also explicitly compute a redirection url to a provider by using the `getRedirectAction` method, in order to create an explicit link for login. For example with Facebook:

#### In Java:

Either you inject the `Config` and `DataStore` in your controller or inherit from the `org.pac4j.play.java.UserProfileController`:

    public Result index() throws Exception {
        Clients clients = config.getClients();
        PlayWebContext context = new PlayWebContext(ctx(), dataStore);
        String urlFacebook = ((FacebookClient) clients.findClient("FacebookClient")).getRedirectAction(context, false).getLocation();
        return ok(views.html.index.render(urlFacebook));
    }

#### In Scala:

You need to use the `Security` trait:

    def index = Action { request =>
      val newSession = getOrCreateSessionId(request)
      val webContext = new PlayWebContext(request, dataStore)
      val clients = config.getClients()
      val urlFacebook = (clients.findClient("FacebookClient").asInstanceOf[FacebookClient]).getRedirectAction(webContext, false).getLocation;
      Ok(views.html.index(urlFacebook)).withSession(newSession)
    }

Notice you need to explicitly call the `getOrCreateSessionId()` in Scala to force the initialization of the data store and attach the returned session to your result.


### Get the user profile

#### In Java:
 
You need to inherit from the `UserProfileController` and call the `getUserProfile()` method.
 
#### In Scala:

You need to extend from the `Security` trait and call the `getUserProfile(request: RequestHeader)` function. 

You can also directly use the `ProfileManager.get(true)` method (`false` not to use the session, but only the current HTTP request) and the `ProfileManager.isAuthenticated()` method. 

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common properties that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:
 
    FacebookProfile facebookProfile = (FacebookProfile) commonProfile;


### Logout

You can log out the current authenticated user using the `ApplicationLogoutController` defined in the `routes` file and by calling the logout url ("/logout"):

    GET  /logout  org.pac4j.play.ApplicationLogoutController.logout()

A blank page is displayed by default unless an *url* parameter is provided. In that case, the user will be redirected to this specified url (if it matches the logout url pattern defined) or to the default logout url otherwise.

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

- `defaultUrl` (optional): the default logout url if the provided *url* parameter does not match the `logoutUrlPattern`
- `logoutUrlPattern` (optional): the logout url pattern that the logout url must match (it's a security check, only relative urls are allowed by default).


## Migration guide

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


## Release notes

See the [release notes](https://github.com/pac4j/play-pac4j/wiki/Release-notes). Learn more by browsing the [play-pac4j Javadoc](http://www.pac4j.org/apidocs/play-pac4j/index.html) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/index.html).


## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)


## Development

The current version 2.0.0-SNAPSHOT is under development.

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/play-pac4j.png?branch=master)](https://travis-ci.org/pac4j/play-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the `resolvers` of your `build.sbt` file:

    resolvers ++= Seq( Resolver.mavenLocal,
        "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
        "Pablo repo" at "https://raw.github.com/fernandezpablo85/scribe-java/mvn-repo/")
