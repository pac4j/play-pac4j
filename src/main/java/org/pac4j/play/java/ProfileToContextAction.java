package org.pac4j.play.java;

import com.google.inject.Inject;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlaySessionStore;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Before CDI you could easily call statically a function from the template engine to access the current user/profile.
 * With CDI there is no way to access the session store by injecting it directly into the the template via CDI.
 * The only way to do this is to pass it as an argument to the template itself, which can be annoying when yo need the profile in an main.tpl for exp. which is called from the other template.
 *
 * <code>
 * @(profile: CommonProfile)
 *
 * ...
 * @main(profile) {
 *   <h1>Hello</h1>
 * }
 * ...
 * </code>
 *
 *
 * Just annotate your controller with the {@link ProfileToContext} annotation and you have the profile in the CTX.
 *
 * <code>
 *   @UserToContextAnnotation
 *    public class UserToContextController extends Controller {
 *      ...
 *    }
 * </code>
 *
 * You can access the Optional for the profile from the template with the following code:
 * <code>
 *   @import Http.Context
 *   @currentUserOptional = @{Context.current().args.get(org.pac4j.play.java.UserToContextAction.CONTEXT_CURRENT_PROFILE_KEY).asInstanceOf[java.util.Optional[org.pac4j.core.profile.CommonProfile]]}
 *   @if(currentUserOptional.isEmpty == false) {
 *     Hello: @currentUserOptional.get().getUsername
 *   }
 *
 * </code>
 *
 * @author Sebastian Hardt (s.hardt@micromata.de)
 */
public class ProfileToContextAction extends Action<ProfileToContext>
{

  /**
   * The current key for the user.
   */
  public static final String CONTEXT_CURRENT_PROFILE_KEY = "pac4j-currentProfile";

  /**
   * The session store of the play application.
   */
  private final PlaySessionStore playSessionStore;

  @Inject
  public ProfileToContextAction(final PlaySessionStore playSessionStore) {
    this.playSessionStore = playSessionStore;
  }

  @Override
  public CompletionStage<Result> call(Http.Context ctx)
  {
    Optional<CommonProfile> userProfile = getUserProfile(ctx,playSessionStore);
    ctx.args.put(CONTEXT_CURRENT_PROFILE_KEY,userProfile);
    return delegate.call(ctx);
  }

  /**
   * Gets the current user profile from the session store.
   * @param ctx the http context.
   * @return the {@link Optional}
   */
  public static Optional<CommonProfile> getUserProfile(Http.Context ctx,final PlaySessionStore playSessionStore) {
    PlayWebContext webContext = new PlayWebContext(ctx,playSessionStore );
    ProfileManager<CommonProfile> profileManager = new ProfileManager(webContext);
    Optional<CommonProfile> profile = profileManager.get(true);
    return profile;
  }
}
