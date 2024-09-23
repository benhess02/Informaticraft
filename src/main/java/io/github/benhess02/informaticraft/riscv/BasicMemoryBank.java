package io.github.benhess02.informaticraft.riscv;

public class BasicMemoryBank implements Memory {

    private final byte[] bytes;

    public BasicMemoryBank(int size) {
        bytes = new byte[size];
    }

    @Override
    public byte read(long address) {
        return bytes[(int)address % bytes.length];
    }

    @Override
    public void write(long address, byte value) {
        bytes[(int)address % bytes.length] = value;
    }
}
