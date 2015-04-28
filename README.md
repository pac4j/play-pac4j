## What is the play-pac4j library ? [![Build Status](https://travis-ci.org/pac4j/play-pac4j.png?branch=1.2.x)](https://travis-ci.org/pac4j/play-pac4j)

The **play-pac4j** library is a *Java and Scala* multi-protocols client for Play framework 2.x.

It supports these 7 authentication mechanisms on client side:

1. OAuth (1.0 & 2.0)
2. CAS (1.0, 2.0, SAML, logout & proxy)
3. HTTP (form & basic auth authentications)
4. OpenID
5. SAML (2.0)
6. GAE UserService
7. OpenID Connect (1.0).

It's available under the Apache 2 license and based on my [pac4j](https://github.com/pac4j/pac4j) library.

<table>
<tr><th>Play framework/ Language</th><th>Java</th><th>Scala</th></tr>
<tr><td>Play 2.0</td><td>play-pac4j_java v1.1.x</td><td>play-pac4j_scala2.9 v1.1.x</td></tr>
<tr><td>Play 2.1</td><td>play-pac4j_java v1.1.x</td><td>play-pac4j_scala2.10 v1.1.x</td></tr>
<tr><td>Play 2.2</td><td>play-pac4j_java v1.2.x</td><td>play-pac4j_scala v1.2.x</td></tr>
<tr><td>Play 2.3</td><td>play-pac4j_java v1.4.x</td><td>play-pac4j_scala2.10 and play-pac4j_scala2.11 v1.4.x</td></tr>
</table>


## Providers supported

<table>
<tr><th>Provider</th><th>Protocol</th><th>Maven dependency</th><th>Client class</th><th>Profile class</th></tr>
<tr><td>CAS server</td><td>CAS</td><td>pac4j-cas</td><td>CasClient & CasProxyReceptor</td><td>CasProfile</td></tr>
<tr><td>CAS server using OAuth Wrapper</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>CasOAuthWrapperClient</td><td>CasOAuthWrapperProfile</td></tr>
<tr><td>DropBox</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>DropBoxClient</td><td>DropBoxProfile</td></tr>
<tr><td>Facebook</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>FacebookClient</td><td>FacebookProfile</td></tr>
<tr><td>GitHub</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>GitHubClient</td><td>GitHubProfile</td></tr>
<tr><td>Google</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>Google2Client</td><td>Google2Profile</td></tr>
<tr><td>LinkedIn</td><td>OAuth 1.0 & 2.0</td><td>pac4j-oauth</td><td>LinkedInClient & LinkedIn2Client</td><td>LinkedInProfile & LinkedIn2Profile</td></tr>
<tr><td>Twitter</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>TwitterClient</td><td>TwitterProfile</td></tr>
<tr><td>Windows Live</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>WindowsLiveClient</td><td>WindowsLiveProfile</td></tr>
<tr><td>WordPress</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>WordPressClient</td><td>WordPressProfile</td></tr>
<tr><td>Yahoo</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>YahooClient</td><td>YahooProfile</td></tr>
<tr><td>PayPal</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>PayPalClient</td><td>PayPalProfile</td></tr>
<tr><td>Vk</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>VkClient</td><td>VkProfile</td></tr>
<tr><td>Foursquare</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>FoursquareClient</td><td>FoursquareProfile</td></tr>
<tr><td>Bitbucket</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>BitbucketClient</td><td>BitbucketProfile</td></tr>
<tr><td>ORCiD</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>OrcidClient</td><td>OrcidProfile</td></tr>
<tr><td>Strava</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>StravaClient</td><td>StravaProfile</td></tr>
<tr><td>Web sites with basic auth authentication</td><td>HTTP</td><td>pac4j-http</td><td>BasicAuthClient</td><td>HttpProfile</td></tr>
<tr><td>Web sites with form authentication</td><td>HTTP</td><td>pac4j-http</td><td>FormClient</td><td>HttpProfile</td></tr>
<tr><td>Yahoo</td><td>OpenID</td><td>pac4j-openid</td><td>YahooOpenIdClient</td><td>YahooOpenIdProfile</td></tr>
<tr><td>SAML Identity Provider</td><td>SAML 2.0</td><td>pac4j-saml</td><td>Saml2Client</td><td>Saml2Profile</td></tr>
<tr><td>Google App Engine User Service</td><td>Gae User Service Mechanism</td><td>pac4j-gae</td><td>GaeUserServiceClient</td><td>GaeUserServiceProfile</td></tr>
<tr><td>OpenID Connect Provider</td><td>OpenID Connect 1.0</td><td>pac4j-oidc</td><td>OidcClient</td><td>OidcProfile</td></tr>
</table>


## Technical description

This library has **just 11 classes**:

* the *Config* class gathers all the configuration
* the *Constants* class gathers all the constants
* the *CallbackController* class is used to finish the authentication process and logout the user
* the *StorageHelper* class deals with storing/retrieving data from the cache
* the *JavaWebContext* class is a Java wrapper for the user request, response and session
* the *JavaController* class is the Java controller to retrieve the user profile or the redirection url to start the authentication process
* the *RequiresAuthentication* annotation is to protect an action if the user is not authenticated and starts the authentication process if necessary
* the *RequiresAuthenticationAction* class is the action to check if the user is not authenticated and starts the authentication process if necessary
* the *ScalaController* trait is the Scala controller to retrieve the user profile or the redirection url to start the authentication process
* the *ScalaWebContext* class is a Scala wrapper for the user request, response and session
* the *PlayLogoutHandler* class is dedicated to CAS support to handle CAS logout request.

and is based on the <i>pac4j-*</i> libraries.

Learn more by browsing the [play-pac4j Javadoc](http://www.pac4j.org/apidocs/play-pac4j/index.html) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/index.html).


## How to use it ?

### Add the required dependencies

First, the dependency on **play-pac4j_java** must be defined in the *build.sbt* file for a Java application:

    libraryDependencies ++= Seq(
      "org.pac4j" % "play-pac4j_java" % "1.2.3"
    )

Or the **play-pac4j_scala** dependency for a Scala application.

For snapshots that are only available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j/), the appropriate resolver must also be defined in the *build.sbt* file:

    resolvers ++= Seq(
      "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
    )

If you want to use a specific client support, you need to add the appropriate dependency:

1. for OAuth support, the *pac4j-oauth* dependency is required
2. for CAS support, the *pac4j-cas* dependency is required
3. for HTTP support, the *pac4j-http* dependency is required
4. for OpenID support, the *pac4j-openid* dependency is required.
5. for SAML 2.0 support, the *pac4j-saml* dependency is required
6. for Google App Engine, the *pac4j-gae* dependency is required.

```
    libraryDependencies ++= Seq(
      "org.pac4j" % "pac4j-http" % "1.7.0",
      "org.pac4j" % "pac4j-cas" % "1.7.0",
      "org.pac4j" % "pac4j-openid" % "1.7.0",
      "org.pac4j" % "pac4j-oauth" % "1.7.0",
      "org.pac4j" % "pac4j-saml" % "1.7.0",
      "org.pac4j" % "pac4j-gae" % "1.7.0",
      "org.pac4j" % "pac4j-oidc" % "1.7.0"
    )
```

### Define the supported clients

To use client support, your application must inherit from the JavaController class for a Java application:

    public class Application extends JavaController {

or from the ScalaController trait for a Scala application:

    object Application extends ScalaController {

You must define all the clients you want to support in the *onStart* method of your Global class for your Java or Scala application:
 
    public void onStart(final Application app) {
      // OAuth
      final FacebookClient facebookClient = new FacebookClient("fb_key", "fb_secret");
      final TwitterClient twitterClient = new TwitterClient("tw_key", "tw_secret");
      // HTTP
      final FormClient formClient = new FormClient("http://localhost:9000/theForm", new SimpleTestUsernamePasswordAuthenticator());
      final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());
      // CAS
      final CasClient casClient = new CasClient();
      // casClient.setLogoutHandler(new PlayLogoutHandler());
      // casClient.setCasProtocol(CasProtocol.SAML);
      // casClient.setGateway(true);
      /*final CasProxyReceptor casProxyReceptor = new CasProxyReceptor();
      casProxyReceptor.setCallbackUrl("http://localhost:9000/casProxyCallback");
      casClient.setCasProxyReceptor(casProxyReceptor);*/
      casClient.setCasLoginUrl("http://localhost:8080/cas/login");
      
      final Clients clients = new Clients("http://localhost:9000/callback", facebookClient, twitterClient, formClient, basicAuthClient, casClient); // , casProxyReceptor);
      Config.setClients(clients);
    }

The */callback* url is the callback url where the providers (Facebook, Twitter, CAS...) redirects the user after successfull authentication (with the appropriate credentials).

### Get user profiles and protect actions

You can get the profile of the (authenticated) user in a Java application by using the *getUserProfile()* method:

    public static Result index() {
      // profile (maybe null if not authenticated)
      final CommonProfile profile = getUserProfile();
      return ok(views.html.index.render(profile));
    }

And protect the access of a specific url by using the *RequiresAuthentication* annotation:

    @RequiresAuthentication(clientName = "FacebookClient")
    public static Result protectedIndex() {
      // profile
      final CommonProfile profile = getUserProfile();
      return ok(views.html.protectedIndex.render(profile));
    }

Or you can get the profile of the (authenticated) user in a Scala application by using the *getUserProfile(request)* method:

    def index = Action { request =>
      val profile = getUserProfile(request)
      Ok(views.html.index(profile))
    }

And protect the access of a specific url by using the *RequiresAuthentication* function:

    def protectedIndex = RequiresAuthentication("FacebookClient") { profile =>
      Action { request =>
        Ok(views.html.protectedIndex(profile))
      }
    }

After successfull authentication, the originally requested url is restored.

### Get redirection urls

You can also explicitely compute a redirection url to a provider for authentication by using the *getRedirectionUrl* method for a Java application:

    public static Result index() {
      final String url = getRedirectionUrl("TwitterClient", "/targetUrl");
      return ok(views.html.index.render(url));
    }

Or in a Scala application (always call the *getOrCreateSessionId(request)* method first):

    def index = Action { request =>
      val newSession = getOrCreateSessionId(request)
      val url = getRedirectionUrl(request, newSession, "FacebookClient", "/targetUrl")
      Ok(views.html.index(url)).withSession(newSession)
    }

### Define the callback url

The callback url must be defined in the *routes* file as well as the logout:

    GET   /                       controllers.Application.index()
    GET   /protected/index.html   controllers.Application.protectedIndex()
    GET   /callback               org.pac4j.play.CallbackController.callback()
    POST  /callback               org.pac4j.play.CallbackController.callback()
    GET   /logout                 org.pac4j.play.CallbackController.logoutAndRedirect()

### Use the appropriate profile

From the *CommonProfile*, you can retrieve the most common properties that all profiles share.
But you can also cast the user profile to the appropriate profile according to the provider used for authentication.
For example, after a Facebook authentication:
 
    // facebook profile
    FacebookProfile facebookProfile = (FacebookProfile) commonProfile;

Or for all the OAuth profiles, to get the access token:

    OAuthProfile oauthProfile = (OAuthProfile) commonProfile
    String accessToken = oauthProfile.getAccessToken();
    // or
    String accessToken = facebookProfile.getAccessToken();</code></pre>

### Demos

Demos with Facebook, Twitter, CAS, form authentication and basic auth authentication providers are available at:

1. [play-pac4j-java-demo](https://github.com/pac4j/play-pac4j-java-demo) for Java applications
2. [play-pac4j-scala-demo](https://github.com/pac4j/play-pac4j-scala-demo) for Scala applications.


## Versions

The current version **1.2.4-SNAPSHOT** is under development. It's available on the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j) as a Maven dependency:

The latest release of the **play-pac4j** project is the **1.2.3** version:

    <dependency>
        <groupId>org.pac4j</groupId>
        <artifactId>play-pac4j_java</artifactId> or <artifactId>play-pac4j_scala</artifactId>
        <version>1.2.3</version>
    </dependency>

See the [release notes](https://github.com/pac4j/play-pac4j/wiki/Release-notes).


## Contact

If you have any question, please use the following mailing lists:
- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)

