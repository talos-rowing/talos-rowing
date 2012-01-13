/*
 * Copyright (c) 2011 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nargila.robostroke.input;

/**
 * Exception tag to wrap exceptions originated from sensor data input implementations
 * @author tshalif
 *
 */
public class InputException extends Exception {

	private static final long serialVersionUID = 6925513526728045138L;

	public InputException() {
	}

	public InputException(String detailMessage) {
		super(detailMessage);
	}

	public InputException(Throwable throwable) {
		super(throwable);
	}

	public InputException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
