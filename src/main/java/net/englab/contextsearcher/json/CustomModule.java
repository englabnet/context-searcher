package net.englab.contextsearcher.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

/**
 * A custom module that provides JSON serializers and deserializers required by the project.
 * Currently, it supports only Guava RangeMap with an Integer as the key.
 */
public class CustomModule extends Module {
    private static final String NAME = "CustomModule";

    @Override
    public String getModuleName() {
        return NAME;
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new CustomSerializers());
        context.addDeserializers(new CustomDeserializers());
        context.addKeyDeserializers(new CustomKeyDeserializers());
    }
}
