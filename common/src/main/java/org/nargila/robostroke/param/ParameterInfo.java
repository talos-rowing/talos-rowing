package org.nargila.robostroke.param;

public interface ParameterInfo {

	public String getId();
	public String getName();
	public String getDescription();
	public String getCategory();
	public ParameterLevel getLevel();
	public Object getDefaultValue();
	public Object[] makeChoices();
	public Object convertFromString(String val);
	public String convertToString(Object val);
	
}
