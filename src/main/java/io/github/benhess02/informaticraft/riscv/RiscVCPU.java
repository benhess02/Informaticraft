package io.github.benhess02.informaticraft.riscv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class RiscVCPU {

    public final Memory memory;

    public final long[] X = new long[32];
    public long PC;

    public long next_pc;
    private int instruction;
    private int opcode;
    private int imm;
    private int rd;
    private int rs1;
    private int rs2;
    private int funct3;
    private int funct7;

    public RiscVCPU(Memory memory) {
        this.memory = memory;
        reset();
    }

    byte read8(long address) {
        return memory.read(address);
    }

    int read8U(long address) {
        return read8(address) & 0xFF;
    }

    void write8(long address, byte value) {
        if(address == 0x10000) {
            System.out.print((char)value);
        } else {
            memory.write(address, value);
        }
    }

    short read16(long address) {
        return (short)(read8U(address) | read8U(address + 1) << 8);
    }

    void write16(long address, short value) {
        write8(address, (byte)value);
        write8(address + 1, (byte)(value >> 8));
    }

    int read32(long address) {
        return read8U(address) | (read8U(address + 1) << 8)
                | (read8U(address + 2) << 16) | (read8U(address + 3) << 24);
    }

    void write32(long address, int value) {
        write8(address, (byte)value);
        write8(address + 1, (byte)(value >> 8));
        write8(address + 2, (byte)(value >> 16));
        write8(address + 3, (byte)(value >> 24));
    }

    long read64(long address) {
        return (long)read8U(address) | ((long)read8U(address + 1) << 8)
                | ((long)read8U(address + 2) << 16) | ((long)read8U(address + 3) << 24)
                | ((long)read8U(address + 4) << 32) | ((long)read8U(address + 5) << 40)
                | ((long)read8U(address + 6) << 48) | ((long)read8U(address + 7) << 56);
    }

    void write64(long address, long value) {
        write8(address, (byte)value);
        write8(address + 1, (byte)(value >> 8));
        write8(address + 2, (byte)(value >> 16));
        write8(address + 3, (byte)(value >> 24));
        write8(address + 4, (byte)(value >> 32));
        write8(address + 5, (byte)(value >> 40));
        write8(address + 6, (byte)(value >> 48));
        write8(address + 7, (byte)(value >> 56));
    }

    void writeReg(int r, long value) {
        /* String[] regNames = new String[] { "zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
                "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
                "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
                "t3", "t4", "t5", "t6" }; */
        if(r != 0) {
            X[r] = value;
            // System.out.println(regNames[r] + " = " + value);
        }
    }

    public void load(ElfFile elf) throws Exception {
        if(elf.objectFileType != 0x02) {
            throw new Exception("ELF is not executable");
        }
        if(elf.targetISA != 0xF3) {
            throw new Exception("target ISA is not RISC-V");
        }

        for(ElfFile.Segment segment : elf.segments) {
            if(segment.segmentType == 0x01) {
                for(int i = 0; i < segment.content.length; i++) {
                    memory.write((int)segment.physicalAddress + i, segment.content[i]);
                }
            }
        }
        PC = (int)elf.entryPoint;
    }

    public void reset() {
        Arrays.fill(X, 0);
        PC = 0;
    }

    public void cycle() {
        instruction = read32(PC);
        next_pc = PC + 4;
        if(!runInstruction()) {
            throw new RuntimeException("invalid instruction: 0x" + Integer.toHexString(instruction).toUpperCase());
        }
        PC = next_pc;
    }

    private void decodeRType() {
        rd = (instruction >>> 7) & 0b00011111;
        funct3 = (instruction >>> 12) & 0b00000111;
        rs1 = (instruction >>> 15) & 0b00011111;
        rs2 = (instruction >>> 20) & 0b00011111;
        funct7 = instruction >>> 25;
    }

    private void decodeIType() {
        rd = (instruction >>> 7) & 0b00011111;
        funct3 = (instruction >>> 12) & 0b00000111;
        rs1 = (instruction >>> 15) & 0b00011111;
        imm = instruction >> 20;
    }

    private void decodeSType() {
        funct3 = (instruction >>> 12) & 0b00000111;
        rs1 = (instruction >>> 15) & 0b00011111;
        rs2 = (instruction >>> 20) & 0b00011111;
        imm = ((instruction >> 25) << 5) | ((instruction >>> 7) & 0b00011111);
    }

    private void decodeBType() {
        funct3 = (instruction >>> 12) & 0b00000111;
        rs1 = (instruction >>> 15) & 0b00011111;
        rs2 = (instruction >>> 20) & 0b00011111;
        imm = ((instruction >> 31) << 12) | (((instruction >>> 7) & 1) << 11)
                | (((instruction >>> 25) & 0b00111111) << 5) | (((instruction >>> 8) & 0b00001111) << 1);
    }

    private void decodeUType() {
        rd = (instruction >>> 7) & 0b00011111;
        imm = instruction & 0b11111111_11111111_11110000_00000000;
    }

    private void decodeJType() {
        rd = (instruction >>> 7) & 0b00011111;
        imm = ((instruction >> 31) << 20) | (instruction & (0xFF << 12))
                | (((instruction >>> 20) & 1) << 11) | (((instruction >>> 21) & 0b00000011_11111111) << 1);
    }

    private boolean runInstruction() {
        return switch (instruction & 0b01111111) {
            case 0b0000011 -> runLoadInstruction();
            case 0b0000111 -> runLoadFPInstruction();
            case 0b0001011 -> false; // custom-0
            case 0b0001111 -> runMiscMemInstruction();
            case 0b0010011 -> runOpImmInstruction();
            case 0b0010111 -> runAUIPCInstruction();
            case 0b0011011 -> runOpImm32Instruction();
            case 0b0011111 -> false; // 48b

            case 0b0100011 -> runStoreInstruction();
            case 0b0100111 -> runStoreFPInstruction();
            case 0b0101011 -> false; // custom-1
            case 0b0101111 -> runAMOInstruction();
            case 0b0110011 -> runOpInstruction();
            case 0b0110111 -> runLuiInstruction();
            case 0b0111011 -> runOp32Instruction();
            case 0b0111111 -> false; // 64b

            case 0b1000011 -> runMAddInstruction();
            case 0b1000111 -> runMSubInstruction();
            case 0b1001011 -> runNMSubInstruction();
            case 0b1001111 -> runNMAddInstruction();
            case 0b1010011 -> runOpFpInstruction();
            case 0b1010111 -> runOpVInstruction();
            case 0b1011011 -> false; // custom-2
            case 0b1011111 -> false; // 48b

            case 0b1100011 -> runBranchInstruction();
            case 0b1100111 -> runJALRInstruction();
            case 0b1101011 -> false; // reserved
            case 0b1101111 -> runJALInstruction();
            case 0b1110011 -> runSystemInstruction();
            case 0b1110111 -> runOpVEInstruction();
            case 0b1111011 -> false; // custom-3
            case 0b1111111 -> false; // 80b
            default -> false;
        };
    }

    private boolean runLoadInstruction() {
        decodeIType();
        long addr = X[rs1] + imm;
        return switch (funct3) {
            case 0b000 -> { // LB
                writeReg(rd, read8(addr));
                yield true;
            }
            case 0b001 -> { // LH
                writeReg(rd, read16(addr));
                yield true;
            }
            case 0b010 -> { // LW
                writeReg(rd, read32(addr));
                yield true;
            }
            case 0b011 -> { // LD
                writeReg(rd, read64(addr));
                yield true;
            }
            case 0b100 -> { // LBU
                writeReg(rd, read8(addr) & 0xFF);
                yield true;
            }
            case 0b101 -> { // LHU
                writeReg(rd, read16(addr) & 0xFFFF);
                yield true;
            }
            case 0b110 -> { // LWU
                writeReg(rd, read32(addr) & 0xFFFFFFFFL);
                yield true;
            }
            default -> false;
        };
    }

    private boolean runStoreInstruction() {
        decodeSType();
        long addr = X[rs1] + imm;
        return switch (funct3) {
            case 0b000 -> { // SB
                write8(addr, (byte)(X[rs2] & 0xFF));
                yield true;
            }
            case 0b001 -> { // SH
                write16(addr, (short)(X[rs2] & 0xFFFF));
                yield true;
            }
            case 0b010 -> { // SW
                write32(addr, (int)(X[rs2] & 0xFFFFFFFFL));
                yield true;
            }
            case 0b011 -> { // SD
                write64(addr, X[rs2]);
                yield true;
            }
            default -> false;
        };
    }

    private boolean runMAddInstruction() {
        return false;
    }

    private boolean runBranchInstruction() {
        decodeBType();
        return switch (funct3) {
            case 0b000 -> { // BEQ
                if(X[rs1] == X[rs2]) {
                    next_pc = PC + imm;
                }
                yield true;
            }
            case 0b001 -> { // BNE
                if(X[rs1] != X[rs2]) {
                    next_pc = PC + imm;
                }
                yield true;
            }
            case 0b100 -> { // BLT
                if(X[rs1] < X[rs2]) {
                    next_pc = PC + imm;
                }
                yield true;
            }
            case 0b101 -> { // BGE
                if(X[rs1] >= X[rs2]) {
                    next_pc = PC + imm;
                }
                yield true;
            }
            case 0b110 -> { // BLTU
                if(Long.compareUnsigned(X[rs1], X[rs2]) < 0) {
                    next_pc = PC + imm;
                }
                yield true;
            }
            case 0b111 -> { // BGTU
                if(Long.compareUnsigned(X[rs1], X[rs2]) >= 0) {
                    next_pc = PC + imm;
                }
                yield true;
            }
            default -> false;
        };
    }

    private boolean runLoadFPInstruction() {
        return false;
    }

    private boolean runStoreFPInstruction() {
        return false;
    }

    private boolean runMSubInstruction() {
        return false;
    }

    private boolean runJALRInstruction() {
        decodeIType();
        next_pc = (X[rs1] + imm) & ~1;
        writeReg(rd, PC + 4);
        return true;
    }

    private boolean runNMSubInstruction() {
        return false;
    }

    private boolean runMiscMemInstruction() { // FENCE
        decodeIType();
        return funct3 == 0 && rd == 0;
    }

    private boolean runAMOInstruction() {
        return false;
    }

    private boolean runNMAddInstruction() {
        return false;
    }

    private boolean runJALInstruction() {
        decodeJType();
        next_pc = PC + imm;
        writeReg(rd, PC + 4);
        return true;
    }

    private boolean runOpImmInstruction() {
        decodeIType();
        return switch (funct3) {
            case 0b000 -> { // ADDI
                writeReg(rd, X[rs1] + imm);
                yield true;
            }
            case 0b010 -> { // SLTI
                if(X[rs1] < imm) {
                    writeReg(rd, 1);
                } else {
                    writeReg(rd, 0);
                }
                yield true;
            }
            case 0b011 -> { // SLTIU
                if(Long.compareUnsigned(X[rs1], imm) < 0) {
                    writeReg(rd, 1);
                } else {
                    writeReg(rd, 0);
                }
                yield true;
            }
            case 0b100 -> { // XORI
                writeReg(rd, X[rs1] ^ imm);
                yield true;
            }
            case 0b110 -> { // ORI
                writeReg(rd, X[rs1] | imm);
                yield true;
            }
            case 0b111 -> { // ANDI
                writeReg(rd, X[rs1] & imm);
                yield true;
            }
            case 0b001 -> { // SLLI
                if((imm & 0b111111000000) != 0) {
                    yield false;
                }
                writeReg(rd, X[rs1] << imm);
                yield true;
            }
            case 0b101 -> switch (imm >>> 6) {
                case 0b000000 -> { // SRLI
                    writeReg(rd, X[rs1] >>> imm);
                    yield true;
                }
                case 0b010000 -> { // SRAI
                    writeReg(rd, X[rs1] >> (imm & 0b111111));
                    yield true;
                }
                default -> false;
            };
            default -> false;
        };
    }

    private boolean runOpInstruction() {
        decodeRType();
        return switch (funct7) {
            case 0b0000000 -> switch (funct3) {
                case 0b000 -> { // ADD
                    writeReg(rd, X[rs1] + X[rs2]);
                    yield true;
                }
                case 0b001 -> { // SLL
                    writeReg(rd, X[rs1] << (X[rs2] & 0b111111));
                    yield true;
                }
                case 0b010 -> { // SLT
                    if(X[rs1] < X[rs2]) {
                        writeReg(rd, 1);
                    } else {
                        writeReg(rd, 0);
                    }
                    yield true;
                }
                case 0b011 -> { // SLTU
                    if (Long.compareUnsigned(X[rs1], X[rs2]) < 0) {
                        writeReg(rd, 1);
                    } else {
                        writeReg(rd, 0);
                    }
                    yield true;
                }
                case 0b100 -> { // XOR
                    writeReg(rd, X[rs1] ^ X[rs2]);
                    yield true;
                }
                case 0b101 -> { // SRL
                    writeReg(rd, X[rs1] >>> (X[rs2] & 0b111111));
                    yield true;
                }
                case 0b110 -> { // OR
                    writeReg(rd, X[rs1] | X[rs2]);
                    yield true;
                }
                case 0b111 -> { // AND
                    writeReg(rd, X[rs1] & X[rs2]);
                    yield true;
                }
                default -> false;
            };
            case 0b0000001 -> switch (funct3) {
                case 0b000 -> { // MUL
                    writeReg(rd, X[rs1] * X[rs2]);
                    yield true;
                }
                case 0b001 -> { // MULH
                    writeReg(rd, Math.multiplyHigh(X[rs1], X[rs2]));
                    yield true;
                }
                case 0b010 -> { // MULHSU
                    long result;
                    if(X[rs1] < 0 && X[rs2] != 0) {
                        result = Math.unsignedMultiplyHigh(-X[rs1], X[rs2]);
                        if(result == 0) {
                            result = ~result;
                        } else {
                            result = -result;
                        }
                    } else {
                        result = Math.unsignedMultiplyHigh(X[rs1], X[rs2]);
                    }
                    writeReg(rd, result);
                    yield true;
                }
                case 0b011 -> { // MULHU
                    writeReg(rd, Math.unsignedMultiplyHigh(X[rs1], X[rs2]));
                    yield true;
                }
                case 0b100 -> { // DIV
                    writeReg(rd, X[rs1] / X[rs2]);
                    yield true;
                }
                case 0b101 -> { // DIVU
                    writeReg(rd, Long.divideUnsigned(X[rs1], X[rs2]));
                    yield true;
                }
                case 0b110 -> { // REM
                    writeReg(rd, X[rs1] % X[rs2]);
                    yield true;
                }
                case 0b111 -> { // REMU
                    writeReg(rd, Long.remainderUnsigned(X[rs1], X[rs2]));
                    yield true;
                }
                default -> false;
            };
            case 0b0100000 -> switch (funct3) {
                case 0b000 -> { // SUB
                    writeReg(rd, X[rs1] - X[rs2]);
                    yield true;
                }
                case 0b101 -> { // SRA
                    writeReg(rd, X[rs1] >> (X[rs2] & 0b111111));
                    yield true;
                }
                default -> false;
            };
            default -> false;
        };
    }

    private boolean runOpFpInstruction() {
        return false;
    }

    private boolean runSystemInstruction() {
        decodeIType();
        if(rd != 0 || rs1 != 0) {
            return false;
        }
        return switch (imm) {
            case 0b000000000000 -> true; // ECALL
            case 0b000000000001 -> true; // EBREAK
            default -> false;
        };
    }

    private boolean runAUIPCInstruction() {
        decodeUType();
        writeReg(rd, PC + imm);
        return true;
    }

    private boolean runLuiInstruction() {
        decodeUType();
        writeReg(rd, imm);
        return true;
    }

    private boolean runOpVInstruction() {
        return false;
    }

    private boolean runOpVEInstruction() {
        return false;
    }

    private boolean runOpImm32Instruction() {
        decodeIType();
        return switch (funct3) {
            case 0b000 -> {
                writeReg(rd, (int)X[rs1] + imm);
                yield true;
            }
            case 0b001 -> { // SLLIW
                if((imm & 0b111111100000) != 0) {
                    yield false;
                }
                writeReg(rd, (int)((X[rs1] << imm) & 0xFFFFFFFFL));
                yield true;
            }
            case 0b101 -> switch (imm >>> 6) {
                case 0b000000 -> { // SRLIW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) >>> imm);
                    yield true;
                }
                case 0b010000 -> { // SRAIW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) >> (imm & 0b111111));
                    yield true;
                }
                default -> false;
            };
            default -> false;
        };
    }

    @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
    private boolean runOp32Instruction() {
        decodeRType();
        return switch (funct7) {
            case 0b0000000 -> switch (funct3) {
                case 0b000 -> { // ADDW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) + (int)(X[rs2] & 0xFFFFFFFFL));
                    yield true;
                }
                case 0b001 -> { // SLLW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) << (X[rs2] & 0b111111));
                    yield true;
                }
                case 0b101 -> { // SRLW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) >>> (X[rs2] & 0b111111));
                    yield true;
                }
                default -> false;
            };
            case 0b0000001 -> switch (funct3) {
                case 0b000 -> { // MULW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) * (int)(X[rs2] & 0xFFFFFFFFL));
                    yield true;
                }
                case 0b100 -> { // DIVW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) / (int)(X[rs2] & 0xFFFFFFFFL));
                    yield true;
                }
                case 0b101 -> { // DIVUW
                    writeReg(rd, (int)((X[rs1] & 0xFFFFFFFFL) / (X[rs2] & 0xFFFFFFFFL)));
                    yield true;
                }
                case 0b110 -> { // REMW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) % (int)(X[rs2] & 0xFFFFFFFFL));
                    yield true;
                }
                case 0b111 -> { // REMUW
                    writeReg(rd, (int)((X[rs1] & 0xFFFFFFFFL) % (X[rs2] & 0xFFFFFFFFL)));
                    yield true;
                }
                default -> false;
            };
            case 0b0100000 -> switch (funct3) {
                case 0b000 -> { // SUBW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) - (int)(X[rs2] & 0xFFFFFFFFL));
                    yield true;
                }
                case 0b101 -> { // SRAW
                    writeReg(rd, (int)(X[rs1] & 0xFFFFFFFFL) >> (X[rs2] & 0b111111));
                    yield true;
                }
                default -> false;
            };
            default -> false;
        };
    }

    public static void main(String[] args) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("C:\\Users\\benhe\\OneDrive\\Desktop\\main.o"));
        ElfFile elf = ElfFile.read(bytes);
        Memory memory = new BasicMemoryBank(0x1000000);
        RiscVCPU cpu = new RiscVCPU(memory);
        cpu.load(elf);
        while(true) {
            //System.out.println("PC: " + Long.toHexString(cpu.PC));
            cpu.cycle();
           /* System.out.println(Long.toHexString(cpu.instruction & 0xFFFFFFFFL));
            for(int r : cpu.X) {
                System.out.print(r + ", ");
            }
            System.out.println();*/
        }
    }
}
