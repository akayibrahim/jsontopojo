package com.iakay.jsontopojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by iakay on 2019-12-07.
 * Thanks to https://github.com/giordamauro/json-to-pojo/blob/master/json-to-pojo/src/main/java/com/mgiorda/JsonToPojo.java
 */

@Mojo(name = "create", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class PojoCreater extends AbstractMojo {

    @Parameter(required = true)
    private String json;

    @Parameter(required = true)
    private String path;

    @Parameter(required = true)
    private String className;

    public void execute() {
        JsonToPojo.fromJson(json, path + "." + className);
    }
}
