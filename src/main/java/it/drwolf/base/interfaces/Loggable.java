package it.drwolf.base.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * It provides an instance of org.slf4j.Logger
 *
 * @author spaladini
 *
 */
public interface Loggable {

	public default Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}

}
