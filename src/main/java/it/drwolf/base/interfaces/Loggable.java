package it.drwolf.base.interfaces;

import play.Logger;
import play.Logger.ALogger;

public interface Loggable {

	public default ALogger getLogger() {
		return Logger.of(this.getClass());
	}

}
