<h2>What is the play-pac4j library ?</h2>

The <b>play-pac4j</b> library is a <i>Java and Scala</i> multi-protocols client for Play framework 2.x.

It supports these 4 protocols on client side : 
<ol>
<li>OAuth (1.0 & 2.0)</li>
<li>CAS (1.0, 2.0, SAML, logout & proxy)</li>
<li>HTTP (form & basic auth authentications)</li>
<li>OpenID.</li>
</ol>

It's available under the Apache 2 license and based on my <a href="https://github.com/leleuj/pac4j">pac4j</a> library.


<h2>Providers supported</h2>

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
<tr><td>Web sites with basic auth authentication</td><td>HTTP</td><td>pac4j-http</td><td>BasicAuthClient</td><td>HttpProfile</td></tr>
<tr><td>Web sites with form authentication</td><td>HTTP</td><td>pac4j-http</td><td>FormClient</td><td>HttpProfile</td></tr>
<tr><td>MyOpenId</td><td>OpenID</td><td>pac4j-openid</td><td>MyOpenIdClient</td><td>MyOpenIdProfile</td></tr>
<tr><td>Google</td><td>OpenID</td><td>pac4j-openid</td><td>GoogleOpenIdClient</td><td>GoogleOpenIdProfile</td></tr>
</table>


<h2>Technical description</h2>

This library has <b>just 11 classes</b> :
<ol>
<li>the <b>Config</b> class gathers all the configuration</li>
<li>the <b>Constants</b> class gathers all the constants</li>
<li>the <b>CallbackController</b> class is used to finish the authentication process and logout the user</li>
<li>the <b>StorageHelper</b> class deals with storing/retrieving data from the cache</li>
<li>the <b>JavaWebContext</b> class is a Java wrapper for the user request, response and session</li>
<li>the <b>JavaController</b> class is the Java controller to retrieve the user profile or the redirection url to start the authentication process</li>
<li>the <b>RequiresAuthentication</b> annotation is to protect an action if the user is not authenticated and starts the authentication process if necessary</li>
<li>the <b>RequiresAuthenticationAction</b> class is the action to check if the user is not authenticated and starts the authentication process if necessary</li>
<li>the <b>ScalaController</b> trait is the Scala controller to retrieve the user profile or the redirection url to start the authentication process</li>
<li>the <b>ScalaWebContext</b> class is a Scala wrapper for the user request, response and session</li>
<li>the <b>PlayLogoutHandler</b> class is dedicated to CAS support to handle CAS logout request.</li>
</ol>

and is based on the <i>pac4j-*</i> libraries.

Learn more by browsing the <a href="http://www.pac4j.org/apidocs/play-pac4j/index.html">play-pac4j Javadoc</a> and the <a href="http://www.pac4j.org/apidocs/pac4j/index.html">pac4j Javadoc</a>.


<h2>How to use it ?</h2>

<h3>Add the required dependencies</h3>

First, the dependency on <b>play-pac4j_java</b> must be defined in the <i>Build.scala</i> file for a Java application :
<pre><code>val appDependencies = Seq(
  "org.pac4j" % "play-pac4j_java" % "1.1.0-SNAPSHOT"
)</code></pre>
Or the <b>play-pac4j_scala2.9</b> dependency for a Scala application in Play framework 2.0 or the <b>play-pac4j_scala2.10</b> dependency for a Scala application in Play framework 2.1.

As it's a snapshot only available in the <a href="https://oss.sonatype.org/content/repositories/snapshots/org/pac4j/">Sonatype Snapshots repository</a>, the appropriate resolver must also be defined in the <i>Build.scala</i> file :
<pre><code>val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
  resolvers += "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
)</code></pre>

If you want to use a specific client support, you need to add the appropriate dependency :
<ul>
<li>for OAuth support, the <i>pac4j-oauth</i> dependency is required</li>
<li>for CAS support, the <i>pac4j-cas</i> dependency is required</li>
<li>for HTTP support, the <i>pac4j-http</i> dependency is required</li>
<li>for OpenID support, the <i>pac4j-openid</i> dependency is required.</li>
</ul>

    val appDependencies = Seq(
        "org.pac4j" % "pac4j-http" % "1.4.2-SNAPSHOT",
        "org.pac4j" % "pac4j-cas" % "1.4.2-SNAPSHOT",
        "org.pac4j" % "pac4j-openid" % "1.4.2-SNAPSHOT",
        "org.pac4j" % "pac4j-oauth" % "1.4.2-SNAPSHOT"
    )


<h3>Define the supported clients</h3>

To use client support, your application must inherit from the JavaController class for a Java application :
<pre><code>public class Application extends JavaController {</code></pre>
or from the ScalaController trait for a Scala application :
<pre><code>object Application extends ScalaController {</code></pre>

You must define all the clients you want to support in the <i>onStart</i> method of your Global class for your Java or Scala application : 
<pre><code>public void onStart(final Application app) {
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
  // OpenID
  final MyOpenIdClient myOpenIdClient = new MyOpenIdClient();
        
  final Clients clients = new Clients("http://localhost:9000/callback", facebookClient, twitterClient, formClient, basicAuthClient, casClient, myOpenIdClient); // , casProxyReceptor);
  Config.setClients(clients);
}</code></pre>

The <i>/callback</i> url is the callback url where the providers (Facebook, Twitter, CAS...) redirects the user after successfull authentication (with the appropriate credentials).

<h3>Get user profiles and protect actions</h3>

You can get the profile of the (authenticated) user in a Java application by using the <i>getUserProfile()</i> method :
<pre><code>public static Result index() {
  // profile (maybe null if not authenticated)
  final CommonProfile profile = getUserProfile();
  return ok(views.html.index.render(profile));
}</code></pre>
And protect the access of a specific url by using the <i>RequiresAuthentication</i> annotation :
<pre><code>@RequiresAuthentication(clientName = "FacebookClient")
public static Result protectedIndex() {
  // profile
  final CommonProfile profile = getUserProfile();
  return ok(views.html.protectedIndex.render(profile));
}</code></pre>

Or you can get the profile of the (authenticated) user in a Scala application by using the <i>getUserProfile(request)</i> method :
<pre><code>def index = Action { request =>
  val profile = getUserProfile(request)
  Ok(views.html.index(profile))
}</code></pre>
And protect the access of a specific url by using the <i>RequiresAuthentication</i> function :
<pre><code>def protectedIndex = RequiresAuthentication("FacebookClient") { profile =>
 Action { request =>
   Ok(views.html.protectedIndex(profile))
 }
}</code></pre>

After successfull authentication, the originally requested url is restored.

<h3>Get redirection urls</h3>

You can also explicitely compute a redirection url to a provider for authentication by using the <i>getRedirectionUrl</i> method for a Java application :
<pre><code>public static Result index() {
  final String url = getRedirectionUrl("TwitterClient", "/targetUrl");
  return ok(views.html.index.render(url));
}</code></pre>
Or in a Scala application (always call the <i>getOrCreateSessionId(request)</i> method first) :
<pre><code>def index = Action { request =>
  val newSession = getOrCreateSessionId(request)
  val url = getRedirectionUrl(request, newSession, "FacebookClient", "/targetUrl")
  Ok(views.html.index(url)).withSession(newSession)
}</code></pre>

<h3>Define the callback url</h3>

The callback url must be defined in the <i>routes</i> file as well as the logout :
<pre><code>GET   /                       controllers.Application.index()
GET   /protected/index.html   controllers.Application.protectedIndex()
GET   /callback               org.pac4j.play.CallbackController.callback()
POST  /callback               org.pac4j.play.CallbackController.callback()
GET   /logout                 org.pac4j.play.CallbackController.logoutAndRedirect()</code></pre>

<h3>Use the appropriate profile</h3>

From the <i>CommonProfile</i>, you can retrieve the most common properties that all profiles share.
But you can also cast the user profile to the appropriate profile according to the provider used for authentication.
For example, after a Facebook authentication : 
<pre><code>// facebook profile
FacebookProfile facebookProfile = (FacebookProfile) commonProfile;</code></pre>
Or for all the OAuth profiles, to get the access token :
<pre><code>OAuthProfile oauthProfile = (OAuthProfile) commonProfile
String accessToken = oauthProfile.getAccessToken();
// or
String accessToken = facebookProfile.getAccessToken();</code></pre>

<h3>Demos</h3>

Demos with Facebook, Twitter, CAS, form authentication, basic auth authentication and myopenid.com providers are available at :
<ul>
<li><a href="https://github.com/leleuj/play-pac4j-java-demo">play-pac4j-java-demo</a> for Java applications</li>
<li><a href="https://github.com/leleuj/play-pac4j-scala-demo">play-pac4j-scala-demo</a> for Scala applications.</li>
</ul>


<h2>Versions</h2>

The current version : <i>1.1.1-SNAPSHOT</i> is under development, it's available in the <a href="https://oss.sonatype.org/content/repositories/snapshots/org/pac4j/">Sonatype snapshots repository</a>.

The latest release of the <b>play-pac4j</b> project is the <b>1.1.0</b> version :
<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;org.pac4j&lt;/groupId&gt;
    &lt;artifactId&gt;play-pac4j_java&lt;/artifactId&gt; or &lt;artifactId&gt;play-pac4j_scala2.9&lt;/artifactId&gt; or &lt;artifactId&gt;play-pac4j_scala2.10&lt;/artifactId&gt;
    &lt;version&gt;1.1.0&lt;/version&gt;
&lt;/dependency&gt;</code></pre>


<h2>Contact</h2>

Find me on <a href="http://www.linkedin.com/in/jleleu">LinkedIn</a> or by email : leleuj@gmail.com
