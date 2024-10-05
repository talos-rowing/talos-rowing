package org.nargila.robostroke;

import java.io.InputStream;
import java.util.Properties;

import org.nargila.robostroke.common.ClockTime;

public class TestProperties  {

  public static class NoSuchProperty extends IllegalArgumentException {

    public NoSuchProperty(String msg) {
      super(msg);
    }
  }

    private final String testName;
    private final Properties testProperties = new Properties();

    public TestProperties() throws Exception {
      this.testName = getClass().getSimpleName();
      String propFileName = testName + ".properties";
    InputStream resourceAsStream = getClass().getResourceAsStream(propFileName);

    if (resourceAsStream == null) {
      throw new Exception("test property file " + propFileName + " not found (package " + getClass().getPackage().getName() + ")");
    }

    testProperties.load(resourceAsStream);
    }


    public static String getCallingMethodName() {
      return getCallingMethodName(null);
    }

    public static String getCallingMethodName(String callee) {

        if (callee == null) {
            callee = "getCallingMethodName";
        }

        StackTraceElement e[] = Thread.currentThread().getStackTrace();
        boolean nextIsCaller = false;

        for (StackTraceElement s : e) {
            System.out.println(s.getMethodName());
            if (nextIsCaller) {
                return s.getMethodName();
            }

            nextIsCaller = s.getMethodName().equals(callee);
        }

        throw new AssertionError("HDIGH: getMethodName failed");
    }


  @SuppressWarnings("unchecked")
  public <T> T v(String name, T defVal) {
        try {
          return (T) v(name, defVal.getClass());
        } catch (NoSuchProperty p) {
          return defVal;
        }
    }

    public String v(String name) {
        String caller = getCallingMethodName("v"); // should return test method name from which v() was called
        return v(caller, name, String.class);
    }

    public <T> T v(String name, Class<T> clazz) {
        String caller = getCallingMethodName("v"); // should return test method name from which v() was called
        return v(caller, name, clazz);
    }

  @SuppressWarnings("unchecked")
  private <T> T v(String caller, String name, Class<T> clazz) {

      String[] candidates = {
          caller + "." + name,
          name
      };

      String val = null;

      for (String varName: candidates) {

        val = testProperties.getProperty(varName);

        if (val != null) {
          break;
        }
      }

      if (val == null) {
        throw new NoSuchProperty("test property " + name + " was not found in " + testName + " for caller '" + caller + "'");
      }

        Object res;

        if (clazz == ClockTime.class) {
            res = ClockTime.fromString(val);
        } else if (clazz == Long.class) {
            res = Long.valueOf(val);
        } else if (clazz == Integer.class) {
            res = Integer.valueOf(val);
        } else if (clazz == String.class) {
            res = val;
        } else if (clazz == Double.class) {
            res = Double.valueOf(val);
        } else {
           throw new AssertionError("unhandled template class " + clazz.getName());
        }

        return (T) res;
    }
}
