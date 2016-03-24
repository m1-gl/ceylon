package com.redhat.ceylon.common.tool;

import com.redhat.ceylon.common.tool.ServiceToolLoader;
import com.redhat.ceylon.common.tool.example.Workaround;

public class TestingToolLoader extends ServiceToolLoader {

    public TestingToolLoader() {
        super(Workaround.class);
    }

    @Override
    public String getToolName(String className) {
        return camelCaseToDashes(className.replaceAll("^(.*\\.)?Test(.*)Tool$", "$2"));
    }

}
