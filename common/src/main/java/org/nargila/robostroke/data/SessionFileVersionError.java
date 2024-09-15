/*
 * Copyright (c) 2024 Tal Shalif
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
package org.nargila.robostroke.data;

import java.io.IOException;

/**
 * Error type for replayed data version mismatch 
 * @author tshalif
 *
 */
public class SessionFileVersionError extends IOException {


	private static final long serialVersionUID = 1L;
	
	private final int version;
	
	public SessionFileVersionError(String message, Throwable cause) {
        super(message, cause);
        version = -1;
    }

    public SessionFileVersionError(String message) {
        super(message);
        version = -1;
    }

    public SessionFileVersionError(Throwable cause) {
        super(cause);
        version = -1;
    }

    public SessionFileVersionError() {
		this(-1);
	}

	public SessionFileVersionError(int version) {
		this.version = version;
	}

	@Override
	public String getMessage() {
		return super.getMessage() == null ? version == -1 ? "session file version number missing" : "incompatible session file version " + version : super.getMessage();
	}
}
