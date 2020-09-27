package com.kyn.ioc;

public class TypedStringValue {
    // value属性值
    private String value;

    // value属性值对应的真正类型（Bean中属性的类型）
    private Class<?> targetType;

    public TypedStringValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public void setTargetType(Class<?> targetType) {
        this.targetType = targetType;
    }

}
