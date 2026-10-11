package com.overpoweredmobs;

/** Persistent piñata age shared when Minecraft converts one mob into another. */
public interface PinataLifetime {
    int opm$getPinataAgeTicks();

    void opm$setPinataAgeTicks(int ticks);
}
