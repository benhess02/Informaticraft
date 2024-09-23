package io.github.benhess02.informaticraft.riscv;

import java.util.ArrayList;

public class ElfFile {

    public boolean isLittleEndian;
    public boolean is64Bit;
    public int targetABI;
    public int abiVersion;
    public int objectFileType;
    public int targetISA;
    public long entryPoint;
    public int flags;

    public ArrayList<Segment> segments;

    public static class Segment {
        public long segmentType;
        public long flags;
        public long virtualAddress;
        public long physicalAddress;
        public long size;
        public long align;
        public byte[] content;

        private Segment() {}
    }

    private ElfFile() {
    }

    public static ElfFile read(byte[] bytes) {
        return new Reader(bytes).readFile();
    }

    private static class Reader {
        private final byte[] bytes;
        private int index;

        public Reader(byte[] bytes) {
            this.bytes = bytes;
            this.index = 0;
        }

        public int read8() {
            if(index < 0 || index >= bytes.length) {
                throw new RuntimeException();
            }
            return bytes[index++] & 0xFF;
        }

        public int read16(boolean littleEndian) {
            int a = read8();
            int b = read8();
            if(littleEndian) {
                return a | (b << 8);
            } else {
                return (a << 8) | b;
            }
        }

        public long read32(boolean littleEndian) {
            int a = read16(littleEndian);
            int b = read16(littleEndian);
            if(littleEndian) {
                return (long)a | ((long)b << 16);
            } else {
                return ((long)a << 16) | (long)b;
            }
        }

        public long read64(boolean littleEndian) {
            long a = read32(littleEndian);
            long b = read32(littleEndian);
            if(littleEndian) {
                return a | (b << 32);
            } else {
                return (a << 32) | b;
            }
        }

        public long readPtr(boolean is64Bit, boolean littleEndian) {
            if(is64Bit) {
                return read64(littleEndian);
            } else {
                return read32(littleEndian);
            }
        }

        public Segment readSegment(boolean is64Bit, boolean littleEndian) {
            Segment segment = new Segment();
            segment.segmentType = read32(littleEndian);
            if(is64Bit) {
                segment.flags = read32(littleEndian);
            }
            long offset = readPtr(is64Bit, littleEndian);
            segment.virtualAddress = readPtr(is64Bit, littleEndian);
            segment.physicalAddress = readPtr(is64Bit, littleEndian);
            long fileSize = readPtr(is64Bit, littleEndian);
            segment.size = readPtr(is64Bit, littleEndian);
            if(!is64Bit) {
                segment.flags = read32(littleEndian);
            }
            segment.align = readPtr(is64Bit, littleEndian);

            segment.content = new byte[(int)fileSize];
            System.arraycopy(bytes, (int)offset, segment.content, 0, (int)fileSize);

            return segment;
        }

        public ElfFile readFile() {
            ElfFile file = new ElfFile();

            // check magic number
            if(read32(false) != 0x7F454C46L) {
                throw new RuntimeException();
            }

            switch(read8()) {
                case 1:
                    file.is64Bit = false;
                    break;
                case 2:
                    file.is64Bit = true;
                    break;
                default:
                    throw new RuntimeException();
            }

            switch(read8()) {
                case 1:
                    file.isLittleEndian = true;
                    break;
                case 2:
                    file.isLittleEndian = false;
                    break;
                default:
                    throw new RuntimeException();
            }

            // version must be 1
            if(read8() != 1) {
                throw new RuntimeException();
            }

            file.targetABI = read8();
            file.abiVersion = read8();

            index += 7; // skip padding

            file.objectFileType = read16(file.isLittleEndian);

            file.targetISA = read16(file.isLittleEndian);

            // version must be 1
            if(read32(file.isLittleEndian) != 1) {
                throw new RuntimeException();
            }


            file.entryPoint = readPtr(file.is64Bit, file.isLittleEndian);

            long programHeaderOffset = readPtr(file.is64Bit, file.isLittleEndian);
            long segmentHeaderOffset = readPtr(file.is64Bit, file.isLittleEndian);


            long flags = read32(file.isLittleEndian);
            int headerSize = read16(file.isLittleEndian);

            int programHeaderEntrySize = read16(file.isLittleEndian);
            int programHeaderNum = read16(file.isLittleEndian);

            file.segments = new ArrayList<>();
            for(int i = 0; i < programHeaderNum; i++) {
                index = (int)programHeaderOffset + i * programHeaderEntrySize;
                file.segments.add(readSegment(file.is64Bit, file.isLittleEndian));
            }

            return file;
        }
    }
}
