package org.nargila.robostroke.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.param.Parameter.BOOLEAN;
import org.nargila.robostroke.param.Parameter.FLOAT;
import org.nargila.robostroke.param.Parameter.INTEGER;

public class ParameterServiceTest {
	
	private static final BOOLEAN BOOLEAN_PARAM = new Parameter.BOOLEAN("bool", null, null, null, true);

	private static final INTEGER INTEGER_PARAM = new Parameter.INTEGER("int", null, null, null, 5);

	private static final FLOAT FLOAT_PARAM = new Parameter.FLOAT("float", null, null, null, .5f);

	private ParameterService ps;
	private final RoboStrokeEventBus bus = new RoboStrokeEventBus();
	
	private final ParameterListenerRegistration[] listenerRegistration = {
			new ParameterListenerRegistration("float", new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter<?> param) {
				}
			}),
			new ParameterListenerRegistration("int", new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter<?> param) {
					intVal = (Integer)param.getValue();					
				}
			}),
			new ParameterListenerRegistration("bool", new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter<?> param) {
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
		ps.registerParam(
				FLOAT_PARAM,
				INTEGER_PARAM,
				BOOLEAN_PARAM);
		
		ps.addListener(listenerRegistration);
	}

	@After
	public void tearDown() throws Exception {
		ps.removeListener(listenerRegistration);
	}

	@Test
	public void testSetParam() {
		assertEquals(.5f, FLOAT_PARAM.getDefaultValue(), 0);
		ps.setParam("float", "0.7");
		assertEquals(.7f, FLOAT_PARAM.getValue(), 0);
		ps.setParam(FLOAT_PARAM, 0.8f);
		assertEquals(.8f, FLOAT_PARAM.getValue(), 0);		
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
				public void onParameterChanged(Parameter<?> param) {
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
