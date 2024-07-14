package org.nargila.robostroke.param;

public class ParameterInfoAlias implements ParameterInfo {
    private final ParameterInfo info;
    private final String alias;

    public ParameterInfoAlias(ParameterInfo info, String alias) {
        this.info = info;
        this.alias = alias;
    }

    @Override
    public String getId() {
        return this.alias;
    }

    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public String getDescription() {
        return info.getDescription();
    }

    @Override
    public String getCategory() {
        return info.getCategory();
    }

    @Override
    public ParameterLevel getLevel() {
        return info.getLevel();
    }

    @Override
    public Object getDefaultValue() {
        return info.getDefaultValue();
    }

    @Override
    public Object[] makeChoices() {
        return info.makeChoices();
    }

    @Override
    public Object convertFromString(String val) {
        return info.convertFromString(val);
    }

    @Override
    public String convertToString(Object val) {
        return info.convertToString(val);
    }

    @Override
    public String[] getAliases() {
        return info.getAliases();
    }
}
