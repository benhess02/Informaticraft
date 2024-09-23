package io.github.benhess02.informaticraft.riscv;

public interface Memory {
    byte read(long address);
    void write(long address, byte value);
}
