<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-play.png" width="300" />
</p>

The `play-pac4j` project is an **easy and powerful security library for Play framework v2** web applications which supports authentication and authorization, but also logout and advanced features like CSRF protection. It can work with Deadbolt.
It's based on Play 2.6 (and Scala 2.11 or Scala 2.12) and on the **[pac4j security engine](https://github.com/pac4j/pac4j) v3**. It's available under the Apache 2 license.

Several versions of the library are available for the different versions of the Play framework:

| Play version | pac4j version | play-pac4j version
|--------------|---------------|-------------------
| 2.0          | 1.7           | [play-pac4j_java 1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Java) / [play-pac4j_scala2.9 1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Scala)
| 2.1          | 1.7           | [play-pac4j_java 1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Java) / [play-pac4j_scala2.10 1.1.x](https://github.com/pac4j/play-pac4j/tree/1.1.x) (Scala)
| 2.2          | 1.7           | [play-pac4j_java 1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x) (Java) / [play-pac4j_scala 1.2.x](https://github.com/pac4j/play-pac4j/tree/1.2.x) (Scala)
| 2.3          | 1.7           | [play-pac4j_java 1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x) (Java) / [play-pac4j_scala2.10](https://github.com/pac4j/play-pac4j/tree/1.4.x) and [play-pac4j_scala2.11 1.4.x](https://github.com/pac4j/play-pac4j/tree/1.4.x) (Scala)
| 2.4          | 1.9           | [play-pac4j 2.3.x](https://github.com/pac4j/play-pac4j/tree/2.3.x) (Java & Scala)
| 2.5          | 2.0           | [play-pac4j 3.0.x](https://github.com/pac4j/play-pac4j/tree/3.0.x) (Java & Scala)
| 2.5          | 2.0           | [play-pac4j_2.11 and play-pac4j_2.12 5.0.x](https://github.com/pac4j/play-pac4j/tree/5.0.x) (Java & Scala)
| 2.6          | 3.0           | 6.0.x (Java & Scala)

**Do NOT use Play 2.6.3 and 2.6.5 versions which have issues in their Cache implementations!**

[**Main concepts and components:**](http://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](http://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for UI authentication while a direct client is for web services authentication:

&#9656; OAuth - SAML - CAS - OpenID Connect - HTTP - OpenID - Google App Engine - LDAP - SQL - JWT - MongoDB - Stormpath - IP address

2) An [**authorizer**](http://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles / permissions - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) The `Secure` annotation / function or the `SecurityFilter` protects an url by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

4) The `CallbackController` finishes the login process for an indirect client

5) The `LogoutController` logs out the user from the application.


Just follow these easy steps to secure your Play 2 web application:


### 1) Add the required dependencies (`play-pac4j` + `pac4j-*` libraries)

You need to add a dependency on:

- the `play-pac4j_2.11` or `play-pac4j_2.12` library: `"org.pac4j" %% "play-pac4j" % "6.0.0-RC2"`
- the appropriate `pac4j` [submodules](http://www.pac4j.org/docs/clients.html) (<em>groupId</em>: **org.pac4j**, *version*: **3.0.0-RC2**): `pac4j-oauth` for OAuth support (Facebook, Twitter...), `pac4j-cas` for CAS support, `pac4j-ldap` for LDAP authentication, etc.

All released artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).

---

### 2) Define the configuration (`Config` + `Client` + `Authorizer` + `PlaySessionStore`)

The configuration (`org.pac4j.core.config.Config`) contains all the clients and authorizers required by the application to handle security.

The `Config` is bound for injection in a `SecurityModule` (or whatever the name you call it):

*In Java:*

```java
public class SecurityModule extends AbstractModule {

    ...

    @Override
    protected void configure() {
        bind(HandlerCache.class).to(Pac4jHandlerCache.class);

        bind(Pac4jRoleHandler.class).to(MyPac4jRoleHandler.class);
        final PlayCacheSessionStore playCacheSessionStore = new PlayCacheSessionStore(getProvider(SyncCacheApi.class));
        //bind(PlaySessionStore.class).toInstance(playCacheSessionStore);
        bind(PlaySessionStore.class).to(PlayCacheSessionStore.class);

        // callback
        final CallbackController callbackController = new CallbackController();
        callbackController.setDefaultUrl("/");
        callbackController.setMultiProfile(true);
        bind(CallbackController.class).toInstance(callbackController);

        // logout
        final LogoutController logoutController = new LogoutController();
        logoutController.setDefaultUrl("/?defaulturlafterlogout");
        //logoutController.setDestroySession(true);
        bind(LogoutController.class).toInstance(logoutController);
    }

    @Provides
    protected FacebookClient provideFacebookClient() {
        final String fbId = configuration.getString("fbId");
        final String fbSecret = configuration.getString("fbSecret");
        return new FacebookClient(fbId, fbSecret);
    }

    @Provides
    protected TwitterClient provideTwitterClient() {
        return new TwitterClient("HVSQGAw2XmiwcKOTvZFbQ", "FSiO9G9VRR4KCuksky0kgGuo8gAVndYymr4Nl7qc8AA");
    }

    @Provides
    protected FormClient provideFormClient() {
        return new FormClient(baseUrl + "/loginForm", new SimpleTestUsernamePasswordAuthenticator());
    }

    ...

    @Provides
    protected SAML2Client provideSaml2Client() {
        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks",
                "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:openidp-feide.xml");
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org");
        cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath());
        return new SAML2Client(cfg);
    }

    @Provides
    protected Config provideConfig(FacebookClient facebookClient, TwitterClient twitterClient, FormClient formClient,
                                   IndirectBasicAuthClient indirectBasicAuthClient, CasClient casClient, SAML2Client saml2Client,
                                   OidcClient oidcClient, ParameterClient parameterClient, DirectBasicAuthClient directBasicAuthClient,
                                   CasProxyReceptor casProxyReceptor, DirectFormClient directFormClient) {
        final Clients clients = new Clients(baseUrl + "/callback", facebookClient, twitterClient, formClient,
                indirectBasicAuthClient, casClient, saml2Client, oidcClient, parameterClient, directBasicAuthClient,
                new AnonymousClient(), casProxyReceptor, directFormClient);

        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer<>("ROLE_ADMIN"));
        config.addAuthorizer("custom", new CustomAuthorizer());
        config.addMatcher("excludedPath", new PathMatcher().excludeRegex("^/facebook/notprotected\\.html$"));
        config.setHttpActionAdapter(new DemoHttpActionAdapter());
        return config;
    }
}
```

See a [full example here](https://github.com/pac4j/play-pac4j-java-demo/blob/master/app/modules/SecurityModule.java).

*In Scala:*

```scala
class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  val baseUrl = configuration.getString("baseUrl").get

  override def configure(): Unit = {

    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/?defaulturlafterlogout")
    callbackController.setMultiProfile(true)
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[LogoutController]).toInstance(logoutController)

    // security components used in controllers
    bind(classOf[SecurityComponents]).to(classOf[DefaultSecurityComponents])
  }

  ...

  @Provides
  def provideCasProxyReceptor: CasProxyReceptor = new CasProxyReceptor()

  @Provides
  def provideCasClient(casProxyReceptor: CasProxyReceptor) = {
    val casConfiguration = new CasConfiguration("http://localhost:8888/cas/login") // ("https://casserverpac4j.herokuapp.com/login")
    casConfiguration.setProtocol(CasProtocol.CAS20)
    casConfiguration.setProxyReceptor(casProxyReceptor)
    new CasClient(casConfiguration)
  }

  @Provides
  def provideOidcClient: OidcClient[OidcProfile] = {
    val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId("343992089165-i1es0qvej18asl33mvlbeq750i3ko32k.apps.googleusercontent.com")
    oidcConfiguration.setSecret("unXK_RSCbCXLTic2JACTiAo9")
    oidcConfiguration.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration")
    oidcConfiguration.addCustomParam("prompt", "consent")
    val oidcClient = new OidcClient[OidcProfile](oidcConfiguration)
    oidcClient.addAuthorizationGenerator(new RoleAdminAuthGenerator)
    oidcClient
  }

  @Provides
  def provideParameterClient: ParameterClient = {
    val jwtAuthenticator = new JwtAuthenticator()
    jwtAuthenticator.addSignatureConfiguration(new SecretSignatureConfiguration("12345678901234567890123456789012"))
    val parameterClient = new ParameterClient("token", jwtAuthenticator)
    parameterClient.setSupportGetRequest(true)
    parameterClient.setSupportPostRequest(false)
    parameterClient
  }

  @Provides
  def provideDirectBasicAuthClient: DirectBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator)

  @Provides
  def provideConfig(facebookClient: FacebookClient, twitterClient: TwitterClient, formClient: FormClient, indirectBasicAuthClient: IndirectBasicAuthClient,
                    casClient: CasClient, saml2Client: SAML2Client, oidcClient: OidcClient[OidcProfile], parameterClient: ParameterClient, directBasicAuthClient: DirectBasicAuthClient,
                    casProxyReceptor: CasProxyReceptor): Config = {
    val clients = new Clients(baseUrl + "/callback", facebookClient, twitterClient, formClient,
      indirectBasicAuthClient, casClient, saml2Client, oidcClient, parameterClient, directBasicAuthClient,
      new AnonymousClient(), casProxyReceptor)

    val config = new Config(clients)
    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer[Nothing]("ROLE_ADMIN"))
    config.addAuthorizer("custom", new CustomAuthorizer)
    config.addMatcher("excludedPath", new PathMatcher().excludeRegex("^/filter/facebook/notprotected\\.html$"))
    config.setHttpActionAdapter(new DemoHttpActionAdapter())
    config
  }
}
```

See a [full example here](https://github.com/pac4j/play-pac4j-scala-demo/blob/master/app/modules/SecurityModule.scala).

`http://localhost:8080/callback` is the url of the callback endpoint, which is only necessary for indirect clients. The `PlayCacheSessionStore` is defined as the implementation for the session store: profiles will be saved in the Play Cache.

Notice that you can also configure a specific `HttpActionAdapter` to handle specific HTTP actions (like redirections, forbidden / unauthorized pages) via the `setHttpActionAdapter` method of the `Config` object. The default available implementation is the `DefaultHttpActionAdapter`, but you can subclass it to define your own HTTP 401 / 403 error pages for example.

Notice that you can also define [matchers](http://www.pac4j.org/docs/matchers.html) via the `addMatcher(name, Matcher)` method.

You can also define a specific `SecurityLogic` via the `setSecurityLogic` method.

---

### 3a) Protect urls per Action (`Secure`)

You can protect (authentication + authorizations) the urls of your Play application by using the `Secure` annotation / function. It has the following behaviour:

1) If the HTTP request matches the `matchers` configuration (or no `matchers` are defined), the security is applied. Otherwise, the user is automatically granted access.

2) First, if the user is not authenticated (no profile) and if some clients have been defined in the `clients` parameter, a login is tried for the direct clients.

3) Then, if the user has a profile, authorizations are checked according to the `authorizers` configuration. If the authorizations are valid, the user is granted access. Otherwise, a 403 error page is displayed.

4) Finally, if the user is still not authenticated (no profile), he is redirected to the appropriate identity provider if the first defined client is an indirect one in the `clients` configuration. Otherwise, a 401 error page is displayed.


The following parameters are available:

1) `clients` (optional): the list of client names (separated by commas) used for authentication:
- in all cases, this filter requires the user to be authenticated. Thus, if the `clients` is blank or not defined, the user must have been previously authenticated
- if the `client_name` request parameter is provided, only this client (if it exists in the `clients`) is selected.

2) `authorizers` (optional): the list of authorizer names (separated by commas) used to check authorizations:
- if the `authorizers` is blank or not defined, no authorization is checked
- the following authorizers are available by default (without defining them in the configuration):
  * `isFullyAuthenticated` to check if the user is authenticated but not remembered, `isRemembered` for a remembered user, `isAnonymous` to ensure the user is not authenticated, `isAuthenticated` to ensure the user is authenticated (not necessary by default unless you use the `AnonymousClient`)
  * `hsts` to use the `StrictTransportSecurityHeader` authorizer, `nosniff` for `XContentTypeOptionsHeader`, `noframe` for `XFrameOptionsHeader `, `xssprotection` for `XSSProtectionHeader `, `nocache` for `CacheControlHeader ` or `securityHeaders` for the five previous authorizers
  * `csrfToken` to use the `CsrfTokenGeneratorAuthorizer` with the `DefaultCsrfTokenGenerator` (it generates a CSRF token and saves it as the `pac4jCsrfToken` request attribute and in the `pac4jCsrfToken` cookie), `csrfCheck` to check that this previous token has been sent as the `pac4jCsrfToken` header or parameter in a POST request and `csrf` to use both previous authorizers.

3) `multiProfile` (optional): it indicates whether multiple authentications (and thus multiple profiles) must be kept at the same time (`false` by default).

4) `matchers` (optional): the list of matcher names (separated by commas) that the request must satisfy to check authentication / authorizations

For example in your controllers:

*In Java:*

```java
@Secure(clients = "FacebookClient")
public Result facebookIndex() {
  return protectedIndexView();
}
```

*In Scala:*

```scala
import org.pac4j.play.scala.Security

class MyController @Inject()(val controllerComponents: SecurityComponents) extends MyBaseController with Security[CommonProfile] {
  def facebookIndex = Secure("FacebookClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  } 
}
```

### 3b) Protect urls via the `SecurityFilter`

In order to protect multiple urls at the same time, you can configure the `SecurityFilter`. You need to configure your application to include the `SecurityFilter` as follows:

First define a `Filters` class in your application (if you have not yet done so).

*In Java:*

```java
package filters;

import org.pac4j.play.filters.SecurityFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

import javax.inject.Inject;

public class Filters implements HttpFilters {

    private final SecurityFilter securityFilter;

    @Inject
    public Filters(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Override
    public EssentialFilter[] filters() {
        return new EssentialFilter[] { securityFilter.asJava() };
    }
}
```

*In Scala:*

```scala
package filters

import javax.inject.Inject
import org.pac4j.play.filters.SecurityFilter
import play.api.http.HttpFilters

class Filters @Inject()(securityFilter: SecurityFilter) extends HttpFilters {

  def filters = Seq(securityFilter)

}
```

Then tell your application to use the filters in `application.conf`:

    play.http.filters = "filters.Filters"

See for more information on the use of filters in Play the [Play documentation on Filters](https://www.playframework.com/documentation/2.6.x/ScalaHttpFilters).

Rules for the security filter can be supplied in application.conf. An example is shown below. It
consists of a list of filter rules, where the key is a regular expression that will be used to
match the url. Make sure that the / is escaped by \\ to make a valid regular expression.

For each regex key, there are three subkeys: `authorizers`, `clients` and `matchers`. Here you can define the
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
        matchers = "excludedPath"
      }}
    ]


### 3c) Working with Deadbolt

While the `play-pac4j` security library can handle authorizations on its own, [Deadbolt](http://deadbolt.ws) is a famous and much used library for authorizations. So you can use it seamlessly with `play-pac4j`.

You need to bind the Deadbolt `HandlerCache` to the `Pac4jHandlerCache` (in your `SecurityModule`):

Also `pac4j` does not require a linked of permissions with roles, you must bind a `Pac4jRoleHandler` handler to correct use of `@RoleBasedPermissions` action.

*In Java:*

```java
import org.pac4j.play.deadbolt2.*;
import be.objectify.deadbolt.java.cache.HandlerCache;

bind(Pac4jRoleHandler.class).to(MyCustomRoleHandler.class);
bind(HandlerCache.class).to(Pac4jHandlerCache.class);
```

*In Scala:*

```scala
import org.pac4j.play.scala.deadbolt2._
import be.objectify.deadbolt.scala.cache.HandlerCache

bind(classOf[Pac4jRoleHandler]).to(classOf[SimpleRoleHandler]) // or MyCustomRoleHandler
bind(classOf[HandlerCache]).to(classOf[Pac4jHandlerCache])
```

Thus, everytime you use a *Deadbolt* handler, the *Deadbolt* subject will be automatically built from the current *pac4j* authenticated user profile, the key used to get the appropriate handler will represent the `clients` parameter used to perform the *pac4j* authentication (like in the `Secure` annotation).

*In Java:*

Example to secure an action in a controller:

```java
@SubjectPresent
```

Example to secure an action in a controller and start a Facebook login process if not authenticated:

```java
@SubjectPresent(handlerKey = "FacebookClient", forceBeforeAuthCheck = true)
```

*In Scala:*

```scala
import org.pac4j.play.scala.deadbolt2.ClientsHandlerKey

somewhereNeeds.key(ClientsHandlerKey("FacebookClient"))
```

---

### 4) Define the callback endpoint only for indirect clients (`CallbackController`)

For indirect clients (like Facebook), the user is redirected to an external identity provider for login and then back to the application.
Thus, a callback endpoint is required in the application. It is managed by the `CallbackController` which has the following behaviour:

1) the credentials are extracted from the current request to fetch the user profile (from the identity provider) which is then saved in the web session

2) finally, the user is redirected back to the originally requested url (or to the `defaultUrl`).


The following parameters are available:

1) `defaultUrl` (optional): it's the default url after login if no url was originally requested (`/` by default)

2) `saveInSession` (optional) : it indicates whether the profile should be saved into the web session (`true` by default)

3) `multiProfile` (optional): it indicates whether multiple authentications (and thus multiple profiles) must be kept at the same time (`false` by default)

4) `defaultClient` (optional): it defines the default client to use to finish the login process if none is provided on the URL (not defined by default).


In the `routes` file:

```properties
GET    /callback    @org.pac4j.play.CallbackController.callback()
```

In the `SecurityModule`:

*In Java:*

```java
CallbackController callbackController = new CallbackController();
callbackController.setDefaultUrl("/");
bind(CallbackController.class).toInstance(callbackController);
```

*In Scala:*

```scala
val callbackController = new CallbackController()
callbackController.setDefaultUrl("/")
bind(classOf[CallbackController]).toInstance(callbackController)
```

---

### 5) Get the user profile (`ProfileManager`)

You can get the profile of the authenticated user using `profileManager.get(true)` (`false` not to use the session, but only the current HTTP request).
You can test if the user is authenticated using `profileManager.isAuthenticated()`.
You can get all the profiles of the authenticated user (if ever multiple ones are kept) using `profileManager.getAll(true)`.

Examples:

*In Java:*

```java
public class Application {

    @Inject
    protected PlaySessionStore playSessionStore;

    public Result getUserProfile() {
        PlayWebContext webContext = new PlayWebContext(ctx(), playSessionStore)
        ProfileManager<CommonProfile> profileManager = new ProfileManager(webContext);
        Optional<CommonProfile> profile = profileManager.get(true);
        ....
    }

}
```

*In Scala:*

```scala
class Application @Inject()(sessionStore: PlaySessionStore, val controllerComponents: ControllerComponents) extends Controller {

    def getUserProfile() = Action { request =>
        val webContext = new PlayWebContext(request, playSessionStore)
        val profileManager = new ProfileManager[CommonProfile](webContext)
        val profile = profileManager.get(true)
        ....
    }
}
```

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common attributes that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:

*In Java:*

```java
FacebookProfile facebookProfile = (FacebookProfile) commonProfile;
```

*In Scala:*

```scala
val facebookProfile = commonProfile.asInstanceOf[FacebookProfile]
```

---

### 6) Logout (`LogoutController`)

The `LogoutController` can handle:

- the local logout by removing the pac4j profiles from the session (it can be used for the front-channel logout from the identity provider in case of a central logout)
- the central logout by calling the identity provider logout endpoint.


It has the following behaviour:

1) If the `localLogout` property is `true`, the pac4j profiles are removed from the web session (and the web session is destroyed if the `destroySession` property is `true`)

2) A post logout action is computed as the redirection to the `url` request parameter if it matches the `logoutUrlPattern` or to the `defaultUrl` if it is defined or as a blank page otherwise

3) If the `centralLogout` property is `true`, the user is redirected to the identity provider for a central logout and
then optionally to the post logout redirection URL (if it's supported by the identity provider and if it's an absolute URL).
If no central logout is defined, the post logout action is performed directly.


The following parameters are available:

1) `defaultUrl` (optional): the default logout url if no `url` request parameter is provided or if the `url` does not match the `logoutUrlPattern` (not defined by default)

2) `logoutUrlPattern` (optional): the logout url pattern that the `url` parameter must match (only relative urls are allowed by default)

3) `localLogout` (optional): whether a local logout must be performed (`true` by default)

4) `destroySession` (optional):  whether we must destroy the web session during the local logout (`false` by default)

5) `centralLogout` (optional): whether a central logout must be performed (`false` by default).

In the `routes` file:

```properties
GET     /logout     @org.pac4j.play.LogoutController.logout()
```

In the `SecurityModule`:

*In Java:*

```java
LogoutController logoutController = new LogoutController();
logoutController.setDefaultUrl("/");
bind(LogoutController.class).toInstance(logoutController);
```

*In Scala:*

```scala
val logoutController = new LogoutController()
logoutController.setDefaultUrl("/")
bind(classOf[LogoutController]).toInstance(logoutController)
```

---

## Migration guide

### 4.x -> 5.0.0

The `play-pac4j` library has been renamed as `play-pac4j_2.11` when built with Scala 2.11 and as `play-pac4j_2.12` when built with Scala 2.12.

Scala trait `Security` was revamped to be more consistent with actions and action builders in Play 2.6.

 - removed function `List[P]=>Action[AnyContent]` and replaced by `AuthenticatedRequest` encapsulating the request and the list of profiles
 - introduced support of any content type not only `AnyContent`
 - `Secure` method no longer returns an `Action`, it returns instance of `ActionBuilder` instead. That enables use of a standard simple action as well
   as use of any parser or even produce an asynchronous result.
 - introduced `SecurityComponents` extending `ControllerComponents` to ease injection of dependencies
 - for backward compatibility, introduced a method `profiles` accepting implicit `AuthenticatedRequest` and returning `List[P]`

### 3.0.0 -> 4.0.0 (Play 2.6)

The `Security` trait extends the `BaseController` and thus, the `controllerComponents` must be injected. The `HttpExecutionContext` no longer needs to be injected into the `Security` trait.

### 2.5.x / 2.6.x -> 3.0.0

The `ApplicationLogoutController` has been renamed as `LogoutController` and the `PlayCacheStore` as `PlayCacheSessionStore`.

### 2.4.0 (Play 2.5) -> 2.5.0 (Play 2.5)

The `SecurityModule` class needs to bind the `PlaySessionStore` to the `PlayCacheStore`.

The `PlayWebContext` needs a `PlaySessionStore`, see examples at heading 5 (Get the user profile (`ProfileManager`)).

### 2.1.0 (Play 2.4) / 2.2.0 (Play 2.5) -> 2.3.0 (Play 2.4) / 2.4.0 (Play 2.5)

The `RequiresAuthentication` annotation and function have been renamed as `Secure` with the `clients` and `authorizers` parameters (instead of `clientName` and `authorizerName`).

The `UserProfileController` class and the `getUserProfile` method in the `Security`  trait no longer exist and the `ProfileManager` must be used instead.

The `ApplicationLogoutController` behaviour has slightly changed: even without any `url` request parameter, the user will be redirected to the `defaultUrl` if it has been defined

### 2.0.1 -> 2.1.0

The separate Scala and Java projects have been merged. You need to change the dependency `play-pac4j-java` or `play-pac4j-scala` to simply `play-pac4j`.

The `getUserProfile` method of the `Security` trait returns a `Option[CommonProfile]` instead of just a `UserProfile`.

### 2.0.0 -> 2.0.1

The `DataStore` concept is replaced by the pac4j `SessionStore` concept. The `PlayCacheStore` does no longer need to be bound in the security module. A new session store could be defined using the `config.setSessionStore` method.

The `DefaultHttpActionAdapter` does not need to be bound in the security module, but must to be set using the `config.setHttpActionAdapter` method.


## Demo

Two demo webapps: [play-pac4j-java-demo](https://github.com/pac4j/play-pac4j-java-demo) & [play-pac4j-scala-demo](https://github.com/pac4j/play-pac4j-scala-demo) are available for tests and implement many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...

Test them online: [http://play-pac4j-java-demo.herokuapp.com](http://play-pac4j-java-demo.herokuapp.com) and [http://play-pac4j-scala-demo.herokuapp.com](http://play-pac4j-scala-demo.herokuapp.com).


## Release notes

See the [release notes](https://github.com/pac4j/play-pac4j/wiki/Release-notes). Learn more by browsing the [play-pac4j_2.11 Javadoc](http://www.javadoc.io/doc/org.pac4j/play-pac4j_2.11/6.0.0) / [play-pac4j_2.12 Javadoc](http://www.javadoc.io/doc/org.pac4j/play-pac4j_2.12/6.0.0) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/3.0.0/index.html).


## Need help?

If you need commercial support (premium support or new/specific features), contact us at [info@pac4j.org](mailto:info@pac4j.org).

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)


## Development

The version 6.0.0-RC3-SNAPSHOT is under development.

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/play-pac4j.png?branch=master)](https://travis-ci.org/pac4j/play-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the `resolvers` of your `build.sbt` file:

```scala
resolvers ++= Seq(Resolver.mavenLocal, "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/")
```
