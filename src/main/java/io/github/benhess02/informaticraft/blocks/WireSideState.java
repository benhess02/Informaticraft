package io.github.benhess02.informaticraft.blocks;

import net.minecraft.util.StringRepresentable;

public enum WireSideState implements StringRepresentable {
    NONE("none"),
    WIRE("wire"),
    PLUG("plug");

    final String name;

    WireSideState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return getSerializedName();
    }
}
