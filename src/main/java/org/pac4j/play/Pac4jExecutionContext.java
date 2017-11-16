package org.pac4j.play;

import com.google.inject.Inject;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;


public class Pac4jExecutionContext extends CustomExecutionContext {

	@Inject
	public Pac4jExecutionContext(ActorSystem arg0) {
		super(arg0, "pac4j-threadpool");
	}

}
