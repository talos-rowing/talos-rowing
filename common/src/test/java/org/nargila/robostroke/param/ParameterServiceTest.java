/*
 * Copyright (c) 2012 Tal Shalif
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
package org.nargila.robostroke.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nargila.robostroke.RoboStrokeEventBus;

public class ParameterServiceTest {
	
	private static final ParameterInfo BOOLEAN_PARAM = new ParameterInfo() {
		
		@Override
		public Object[] makeChoices() {
			return null;
		}
		
		@Override
		public String getName() {
			return null;
		}
		
		@Override
		public ParameterLevel getLevel() {
			return null;
		}
		
		@Override
		public String getId() {
			return "bool";
		}
		
		@Override
		public String getDescription() {
			return null;
		}
		
		@Override
		public Object getDefaultValue() {
			return true;
		}
		
		@Override
		public String getCategory() {
			return null;
		}
		
		@Override
		public String convertToString(Object val) {
			return String.valueOf(val);
		}
		
		@Override
		public Object convertFromString(String val) {
			return new Boolean(val);
		}

		@Override
		public String[] getAliases() {
			return new String[0];
		}
	};

	private static final ParameterInfo INTEGER_PARAM = new ParameterInfo() {
		
		@Override
		public Object[] makeChoices() {
			return null;
		}
		
		@Override
		public String getName() {
			return null;
		}
		
		@Override
		public ParameterLevel getLevel() {
			return null;
		}
		
		@Override
		public String getId() {
			return "int";
		}
		
		@Override
		public String getDescription() {
			return null;
		}
		
		@Override
		public Object getDefaultValue() {
			return 5;
		}
		
		@Override
		public String getCategory() {
			return null;
		}
		
		@Override
		public String convertToString(Object val) {
			return String.valueOf(val);
		}
		
		@Override
		public Object convertFromString(String val) {
			return new Integer(val);
		}

		@Override
		public String[] getAliases() {
			return new String[0];
		}
	};

	private static final ParameterInfo FLOAT_PARAM = new ParameterInfo() {
		
		@Override
		public Object[] makeChoices() {
			return null;
		}
		
		@Override
		public String getName() {
			return null;
		}
		
		@Override
		public ParameterLevel getLevel() {
			return null;
		}
		
		@Override
		public String getId() {
			return "float";
		}
		
		@Override
		public String getDescription() {
			return null;
		}
		
		@Override
		public Object getDefaultValue() {
			return 0.5f;
		}
		
		@Override
		public String getCategory() {
			return null;
		}
		
		@Override
		public String convertToString(Object val) {
			return String.valueOf(val);
		}
		
		@Override
		public Object convertFromString(String val) {
			return new Float(val);
		}

		@Override
		public String[] getAliases() {
			return new String[0];
		}
	};

	
	Parameter f = new Parameter(FLOAT_PARAM);
	Parameter i = new Parameter(INTEGER_PARAM);
	Parameter b = new Parameter(BOOLEAN_PARAM);

	private ParameterService ps;
	private final RoboStrokeEventBus bus = new RoboStrokeEventBus();
	
	private final ParameterListenerRegistration[] listenerRegistration = {
			new ParameterListenerRegistration("float", new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter param) {
				}
			}),
			new ParameterListenerRegistration("int", new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter param) {
					intVal = (Integer)param.getValue();					
				}
			}),
			new ParameterListenerRegistration("bool", new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter param) {
				}
			})
	};
	
	private int intVal = -1;
	
	@BeforeClass
	public static void beforeClass() {
		
	}
	
	@Before
	public void setUp() throws Exception {
		ps = new ParameterService(bus);
		ps.registerParam(f, i, b);
		
		ps.addListener(listenerRegistration);
	}

	@After
	public void tearDown() throws Exception {
		ps.removeListener(listenerRegistration);
	}

	@Test
	public void testSetParam() {
		assertEquals(.5f, (Float)f.getDefaultValue(), 0);
		ps.setParam("float", "0.7");
		assertEquals(.7f, (Float)f.getValue(), 0);
		ps.setParam(f, 0.8f);
		assertEquals(.8f, (Float)f.getValue(), 0);		
	}
	
	@Test
	public void testSetParamViaListener() {
		ps.setParam("int", "7");
		assertEquals(7, intVal);
		ps.setParam("int", "8");
		assertEquals(8, intVal);
	}

	@Test
	public void testRecursionDetection() {
		ParameterListenerRegistration plr = 
			new ParameterListenerRegistration("bool", new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter param) {
					ps.setParam("int", "0");					
				}
			});

		ps.addListener(plr);
		
		try {
			ps.setParam("int", "666");
			assertEquals(666, intVal);	
			
			try {
				ps.setParam("bool", "false");
				fail("IllegalStateException should have been thrown due to recursive parameter setting from inside listener");
			} catch (IllegalStateException e) {
				
			}
		} finally {
			ps.removeListener(plr);
		}
		ps.setParam("int", "8");
		assertEquals(8, intVal);
	}
}
