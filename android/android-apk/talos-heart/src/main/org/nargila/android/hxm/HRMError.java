/* 
 * Copyright (C) 2010 Tal Shalif
 * 
 * This file is part of robostroke HRM.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nargila.android.hxm;

public class HRMError extends Exception {
	private static final long serialVersionUID = 1166534865451645533L;
	
	public final ErrorCode code;
	
	public HRMError(ErrorCode code, String msg, Throwable cause) {
		super(msg, cause);
		
		this.code = code;
	}
	
	public HRMError(ErrorCode code, String msg) {
		this(code, msg, null);
	}
}
