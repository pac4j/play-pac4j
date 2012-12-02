<h2>What is Play OAuth client ?</h2>

<b>play-oauth-client</b> is a <i>Java and Scala</i> OAuth client for Play framework 2.0 to :
<ol>
<li>delegate authentication and permissions to an OAuth provider (i.e. the user is redirected to the OAuth provider to log in)</li>
<li>(in the application) retrieve the profile of the authorized user after successfull authentication and permissions acceptation (at the OAuth provider).</li>
</ol>

It's available under the Apache 2 license and based on my <a href="https://github.com/leleuj/scribe-up">scribe-up</a> library (which deals with OAuth authentication and user profile retrieval).

<h2>OAuth providers supported</h2>

<table>
<tr><td>Web site</td><td>Protocol</td><td>Provider</td><td>Profile</td></tr>
<tr><td>sites using OAuth Wrapper for CAS server</td><td>OAuth 2.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/CasOAuthWrapperProvider.html">CasOAuthWrapperProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/casoauthwrapper/CasOAuthWrapperProfile.html">CasOAuthWrapperProfile</a></td></tr>
<tr><td>DropBox</td><td>OAuth 1.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/DropBoxProvider.html">DropBoxProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/dropbox/DropBoxProfile.html">DropBoxProfile</a></td></tr>
<tr><td>Facebook</td><td>OAuth 2.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/FacebookProvider.html">FacebookProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/facebook/FacebookProfile.html">FacebookProfile</a></td></tr>
<tr><td>Github</td><td>OAuth 2.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/GitHubProvider.html">GitHubProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/github/GitHubProfile.html">GitHubProfile</a></td></tr>
<tr><td>Google</td><td>OAuth 1.0 & 2.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/GoogleProvider.html">GoogleProvider</a> & <a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/Google2Provider.html">Google2Provider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/google/GoogleProfile.html">GoogleProfile</a> & <a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/google2/Google2Profile.html">Google2Profile</a></td></tr>
<tr><td>LinkedIn</td><td>OAuth 1.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/LinkedInProvider.html">LinkedInProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/linkedin/LinkedInProfile.html">LinkedInProfile</a></td></tr>
<tr><td>Twitter</td><td>OAuth 1.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/TwitterProvider.html">TwitterProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/twitter/TwitterProfile.html">TwitterProfile</a></td></tr>
<tr><td>Windows Live</td><td>OAuth 2.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/WindowsLiveProvider.html">WindowsLiveProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/windowslive/WindowsLiveProfile.html">WindowsLiveProfile</a></td></tr>
<tr><td>WordPress</td><td>OAuth 2.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/WordPressProvider.html">WordPressProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/wordpress/WordPressProfile.html">WordPressProfile</a></td></tr>
<tr><td>Yahoo</td><td>OAuth 1.0</td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/provider/impl/YahooProvider.html">YahooProvider</a></td><td><a href="http://javadoc.leleuj.cloudbees.net/scribe-up/1.3.0-SNAPSHOT/org/scribe/up/profile/yahoo/YahooProfile.html">YahooProfile</a></td></tr>
</table>

Follow the guide to <a href="https://github.com/leleuj/scribe-up/wiki/Extend-or-add-a-new-provider">extend or add a new provider</a>.

<h2>Technical description</h2>

This library has <b>just 9 classes</b> :
<ol>
<li>the <b>OAuthConfiguration</b> class gathers all the OAuth configuration</li>
<li>the <b>OAuthConstants</b> class gathers all the OAuth constants</li>
<li>the <b>OAuthController</b> class is to finish the OAuth authentication process and logout the user</li>
<li>the <b>JavaUserSession</b> class is a Java wrapper for the user session</li>
<li>the <b>OAuthJavaController</b> class is the Java controller to retrieve the user profile or the redirect url to start the OAuth authentication process</li>
<li>the <b>RequiresOAuthAuthentication</b> annotation is to protect an action if the user is not "OAuth authenticated" and starts the OAuth authentication process if necessary</li>
<li>the <b>RequiresOAuthAuthenticationAction</b> class is the action to check if the user is not "OAuth authenticated" and starts the OAuth authentication process if necessary</li>
<li>the <b>OAuthScalaController</b> trait is the Scala controller to retrieve the user profile or the redirect url to start the OAuth authentication process</li>
<li>the <b>ScalaUserSession</b> class is a Scala wrapper for the user session</li>
</ol>

and the <a href="https://github.com/leleuj/scribe-up">scribe-up</a> library.

<h2>How to use it ?</h2>

First, the dependency on <b>play-oauth-client</b> must be defined in the <i>Build.scala</i> file :
<pre><code>    val appDependencies = Seq(
      "com.github.leleuj.play.oauth.client" % "play-oauth-client" % "1.0.0-SNAPSHOT"
    )</code></pre>
with the appropriate resolver :
<pre><code>    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
    )</code></pre>

To use the OAuth integration, your application must inherit from the OAuthJavaController class for a Java application :
<pre><code>public class Application extends OAuthJavaController {</code></pre>
or from the OAuthScalaController trait for a Scala application :
<pre><code>object Application extends OAuthScalaController {</code></pre>

If you want to authenticate at Facebook, Twitter..., you have to define the providers in the static initializer of your application for your Java application :
<pre><code>static {
  final FacebookProvider facebookProvider = new FacebookProvider();
  facebookProvider.setKey("my_fb_key");
  facebookProvider.setSecret("my_fb_secret");
  final TwitterProvider twitterProvider = new TwitterProvider();
  twitterProvider.setKey("my_tw_key");
  twitterProvider.setSecret("my_tw_secret");
  OAuthConfiguration.init("http://localhost:9000/play-oauth", facebookProvider, twitterProvider);
}</code></pre>
or in the constructor for your Scala application :
<pre><code>  private val facebookProvider = new FacebookProvider()
  facebookProvider.setKey("my_fb_key")
  facebookProvider.setSecret("my_fb_secret")
  private val twitterProvider = new TwitterProvider()
  twitterProvider.setKey("my_tw_key")
  twitterProvider.setSecret("my_tw_secret")
  OAuthConfiguration.init("http://localhost:9000/play-oauth", facebookProvider, twitterProvider)</code></pre>
  
The <i>/play-oauth</i> url is the callback url where the OAuth provider (Facebook, Twitter...) redirects the user after successfull authentication.

Then, you can get the OAuth profile of the (authenticated) user in a Java application by using the <i>profile()</i> method :
<pre><code>public static Result index() {
  // oauth profile (maybe null if not authenticated)
  final UserProfile userProfile = profile();
  return ok(views.html.index.render(userProfile));
}</code></pre>
And protect the access of a specific url by using the <i>RequiresOAuthAuthentication</i> annotation :
<pre><code>@RequiresOAuthAuthentication(providerType = "FacebookProvider")
public static Result protectedIndex() {
  // oauth profile
  final UserProfile userProfile = profile();
  return ok(views.html.protectedIndex.render(userProfile));
}</code></pre>

Or you can get the OAuth profile of the (authenticated) user in a Scala application by using the <i>OAuthProfile</i> function :
<pre><code>def index = OAuthProfile { profile =>
  Action { request =>
    Ok(views.html.index(profile))
  }
}</code></pre>
And protect the access of a specific url by using the <i>RequiresOAuthAuthentication</i> function :
<pre><code>def protectedIndex = RequiresOAuthAuthentication("FacebookProvider") { profile =>
 Action { request =>
   Ok(views.html.protectedIndex(profile))
 }
}</code></pre>

After successfull OAuth authentication, the originally requested url is re-called.

You can also explicitely compute a redirection url to an OAuth provider for authentication by using the <i>redirectUrl()</i> method for a Java application :
<pre><code>public static Result index() {
  final String url = redirectUrl("TwitterProvider");
  return ok(views.html.index.render(url));
}</code></pre>
Or in a Scala application :
<pre><code>def index = Action { request =>
  val (url, newSession) = redirectUrl(request, "TwitterProvider")
  Ok(views.html.index(url)).withSession(newSession)
}</code></pre>

The callback OAuth url must be also defined in the <i>routes</i> file as well as the logout :
<pre><code>GET   /                       controllers.Application.index()
GET   /protected/index.html   controllers.Application.protectedIndex()
GET   /play-oauth             com.github.leleuj.play.oauth.client.OAuthController.callback()
GET   /logout                 com.github.leleuj.play.oauth.client.OAuthController.logoutAndRedirect()</code></pre>

As a user profile, you can have a specific profile for Facebook or a common profile for all providers :
<pre><code>// user profile
UserProfile userProfile = profile();
// facebook profile
FacebookProfile facebookProfile = (FacebookProfile) userProfile;
// common profile to all providers
CommonProfile commonProfile = (CommonProfile) userProfile;</code></pre>
If you want to interact more with the OAuth provider, you can retrieve the access token from the (OAuth) profile :
<pre><code>OAuthProfile oauthProfile = (OAuthProfile) userProfile;
String accessToken = oauthProfile.getAccessToken();
// or
String accessToken = facebookProfile.getAccessToken();</code></pre>

Demos with Facebook and Twitter providers are available at :
- <a href="https://github.com/leleuj/play-oauth-client-java-demo">play-oauth-client-java-demo</a> for Java applications
- <a href="https://github.com/leleuj/play-oauth-client-scala-demo">play-oauth-client-scala-demo</a> for Scala applications.

<h2>Versions</h2>

The current version : <i>1.0.0-SNAPSHOT</i> is under development.

<h2>Contact</h2>

Find me on <a href="http://www.linkedin.com/in/jleleu">LinkedIn</a> or by email : leleuj@gmail.com
