/*
 * MultiExu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import cyclix.STREAM_PREF_IMPL
import cyclix.hw_stage
import hwast.*

enum class REORDEX_MODE {
    COPROCESSOR,
    RISC
}

open class Reordex_CFG(val RF_width : Int,
                       val ARF_depth : Int,
                       val rename_RF: Boolean,
                       val PRF_depth : Int,
                       val trx_inflight_num : Int,
                       val mode : REORDEX_MODE) {

    val ARF_addr_width = GetWidthToContain(ARF_depth)
    val PRF_addr_width = GetWidthToContain(PRF_depth)

    var req_struct = hw_struct("req_struct")
    var resp_struct = hw_struct("resp_struct")

    var src_imms = ArrayList<hw_var>()
    fun AddSrcImm(name : String, new_type : hw_type) : hw_var {
        var new_var = hw_var(name, new_type, "0")
        req_struct.add(name, new_type, "0")
        src_imms.add(new_var)
        return new_var
    }
    fun AddSrcUImm(name : String, new_width : Int) : hw_var {
        return AddSrcImm(name, hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(new_width)))
    }
    fun AddSrcSImm(name : String, new_width : Int) : hw_var {
        return AddSrcImm(name, hw_type(DATA_TYPE.BV_SIGNED, hw_dim_static(new_width)))
    }

    var dst_imms = ArrayList<hw_var>()
    fun AddDstImm(name : String, new_type : hw_type) : hw_var {
        var new_var = hw_var(name, new_type, "0")
        resp_struct.add(name, new_type, "0")
        dst_imms.add(new_var)
        return new_var
    }
    fun AddDstUImm(name : String, new_width : Int) : hw_var {
        return AddDstImm(name, hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(new_width)))
    }
    fun AddDstSImm(name : String, new_width : Int) : hw_var {
        return AddDstImm(name, hw_type(DATA_TYPE.BV_SIGNED, hw_dim_static(new_width)))
    }

    var rss = ArrayList<hw_var>()
    fun AddRs() : hw_var {
        var new_var = hw_var("rs" + rss.size, RF_width-1, 0, "0")
        req_struct.addu("rs" + rss.size + "_rdata", RF_width-1, 0, "0")
        rss.add(new_var)
        return new_var
    }

    var rds = ArrayList<hw_var>()
    fun AddRd() : hw_var {
        var new_var = hw_var("rd" + rds.size, RF_width-1, 0, "0")
        req_struct.addu("rd" + rds.size + "_tag", RF_width-1, 0, "0")
        rds.add(new_var)
        return new_var
    }

    init {
        req_struct.addu("trx_id",     31, 0, "0")       // TODO: clean up
        resp_struct.addu("trx_id",     31, 0, "0")      // TODO: clean up
        resp_struct.addu("tag",     31, 0, "0")         // TODO: clean up
        resp_struct.addu("wdata",     RF_width-1, 0, "0")
    }
}

open class RISCDecodeContainer (MultiExu_CFG : Reordex_CFG) : hw_astc_stdif() {

    var RootExec = hw_exec(hw_opcode("TEST"))

    init {
        add(RootExec)
    }
}

class RISCDecodeLogic (MultiExu_CFG : Reordex_CFG) : RISCDecodeContainer(MultiExu_CFG) {

    var instr_code = ugenvar("instr_code", 31, 0, "0")

    //// base opcodes ///////////

    val opcode_LOAD			= 0x03
    val opcode_LOAD_FP		= 0x07
    val opcode_MISC_MEM		= 0x0f
    val opcode_OP_IMM		= 0x13
    val opcode_AUIPC		= 0x17
    val opcode_OP_IMM_32	= 0x1b
    val opcode_STORE		= 0x23
    val opcode_STORE_FP		= 0x27
    val opcode_AMO			= 0x2f
    val opcode_OP			= 0x33
    val opcode_LUI			= 0x37
    val opcode_OP_32		= 0x3b
    val opcode_MADD			= 0x43
    val opcode_MSUB			= 0x47
    val opcode_NMSUB		= 0x4b
    val opcode_NMADD		= 0x4f
    val opcode_OP_FP		= 0x53
    val opcode_BRANCH		= 0x63
    val opcode_JALR			= 0x67
    val opcode_JAL			= 0x6f
    val opcode_SYSTEM		= 0x73

    val instrcode_MRET        = 0x30200073

    // ALU opcodes
    val aluop_ADD		= 0
    val aluop_SUB		= 1
    val aluop_AND		= 2
    val aluop_OR		= 3
    val aluop_SLL		= 4
    val aluop_SRL		= 5
    val aluop_SRA		= 6
    val aluop_XOR		= 7
    val aluop_CLRB		= 8
    val aluop_SLT		= 9

    // op1 sources
    val OP0_SRC_RS1     = 0
    val OP0_SRC_IMM     = 1
    val OP0_SRC_PC 	    = 2
    // op2 sources
    val OP1_SRC_RS2     = 0
    val OP1_SRC_IMM     = 1
    val OP1_SRC_CSR     = 2

    // rd sources
    val RD_LUI		    = 0
    val RD_ALU		    = 1
    val RD_CF_COND	    = 2
    val RD_OF_COND	    = 3
    val RD_PC_INC	    = 4
    val RD_MEM		    = 5
    val RD_CSR		    = 6

    // jmp sources
    val JMP_SRC_IMM     = 0
    val JMP_SRC_ALU     = 1

    var opcode          = ugenvar("opcode", 6, 0, "0")
    val curinstr_addr   = ugenvar("curinstr_addr_decoder", 31, 0, "0")

    ///////////////////////

    // control transfer signals
    var jump_req        = ugenvar("jump_req", 0, 0, "0")
    var jump_req_cond   = ugenvar("jump_req_cond", 0, 0, "0")
    var jump_src        = ugenvar("jump_src", 0, 0, JMP_SRC_IMM.toString())
    var jump_vector     = ugenvar("jump_vector", 31, 0, "0")

    // regfile control signals
    var rs0_req         = ugenvar("rs0_req", 0, 0, "0")
    var rs0_addr        = ugenvar("rs0_addr", 4, 0, "0")
    var rs0_rdata       = ugenvar("rs0_rdata", 31, 0, "0")

    var rs1_req         = ugenvar("rs1_req", 0, 0, "0")
    var rs1_addr        = ugenvar("rs1_addr", 4, 0, "0")
    var rs1_rdata       = ugenvar("rs1_rdata", 31, 0, "0")

    //var rs2_req         = AdduLocal("rs2_req", 0, 0, "0")
    //var rs2_addr        = AdduLocal("rs2_addr", 4, 0, "0")
    //var rs2_rdata       = AdduLocal("rs2_rdata", 31, 0, "0")

    var csr_rdata       = ugenvar("csr_rdata", 31, 0, "0")

    var rd_req          = ugenvar("rd_req", 0, 0, "0")
    var rd_source       = ugenvar("rd_source", 2, 0, RD_ALU.toString())
    var rd_addr         = ugenvar("rd_addr", 4, 0, "0")
    var rd_wdata        = ugenvar("rd_wdata", 31, 0, "0")
    var rd_rdy          = ugenvar("rd_rdy", 0, 0, "0")

    var immediate_I     = ugenvar("immediate_I", 31, 0, "0")
    var immediate_S     = ugenvar("immediate_S", 31, 0, "0")
    var immediate_B     = ugenvar("immediate_B", 31, 0, "0")
    var immediate_U     = ugenvar("immediate_U", 31, 0, "0")
    var immediate_J     = ugenvar("immediate_J", 31, 0, "0")

    var immediate       = ugenvar("immediate", 31, 0, "0")

    var curinstraddr_imm    = ugenvar("curinstraddr_imm", 31, 0, "0")

    var funct3          = ugenvar("funct3", 2, 0, "0")
    var funct7          = ugenvar("funct7", 6, 0, "0")
    var shamt           = ugenvar("shamt", 4, 0, "0")

    var fencereq        = ugenvar("fencereq", 0, 0, "0")
    var pred            = ugenvar("pred", 3, 0, "0")
    var succ            = ugenvar("succ", 3, 0, "0")

    var ecallreq        = ugenvar("ecallreq", 0, 0, "0")
    var ebreakreq       = ugenvar("ebreakreq", 0, 0, "0")

    var csrreq          = ugenvar("csrreq", 0, 0, "0")
    var csrnum          = ugenvar("csrnum", 11, 0, "0")
    var zimm            = ugenvar("zimm", 4, 0, "0")

    var op0_source      = ugenvar("op0_source", 1, 0, OP0_SRC_RS1.toString())
    var op1_source      = ugenvar("op1_source", 1, 0, OP1_SRC_RS2.toString())

    // ALU control
    var alu_req         = ugenvar("alu_req", 0, 0, "0")
    var alu_op1         = ugenvar("alu_op1", 31, 0, "0")
    var alu_op2         = ugenvar("alu_op2", 31, 0, "0")
    var alu_op1_wide    = ugenvar("alu_op1_wide", 32, 0, "0")
    var alu_op2_wide    = ugenvar("alu_op2_wide", 32, 0, "0")
    var alu_opcode      = ugenvar("alu_opcode", 3, 0, "0")
    var alu_unsigned    = ugenvar("alu_unsigned", 0, 0, "0")

    var alu_result_wide = ugenvar("alu_result_wide", 32, 0, "0")
    var alu_result      = ugenvar("alu_result", 31, 0, "0")
    var alu_CF          = ugenvar("alu_CF", 0, 0, "0")
    var alu_SF          = ugenvar("alu_SF", 0, 0, "0")
    var alu_ZF          = ugenvar("alu_ZF", 0, 0, "0")
    var alu_OF          = ugenvar("alu_OF", 0, 0, "0")
    var alu_overflow    = ugenvar("alu_overflow", 0, 0, "0")

    // data memory control
    var mem_req         = ugenvar("mem_req", 0, 0, "0")
    var mem_cmd         = ugenvar("mem_cmd", 0, 0, "0")
    var mem_addr        = ugenvar("mem_addr", 31, 0, "0")
    var mem_be          = ugenvar("mem_be", 3, 0, "0")
    var mem_wdata       = ugenvar("mem_wdata", 31, 0, "0")
    var mem_rdata       = ugenvar("mem_rdata", 31, 0, "0")
    var mem_rshift      = ugenvar("mem_rshift", 0, 0, "0")
    var load_signext    = ugenvar("load_signext", 0, 0, "0")

    var mret_req        = ugenvar("mret_req", 0, 0, "0")
    var MRETADDR        = ugenvar("MRETADDR", 31, 0, "0")

    //////////
    var rs0_rdy         = ugenvar("rs0_rdy", 0, 0, "0")
    var rs0_tag         = ugenvar("rs0_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")

    var rs1_rdy         = ugenvar("rs1_rdy", 0, 0, "0")
    var rs1_tag         = ugenvar("rs1_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")

    var rd_tag          = ugenvar("rd0_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")

    init {
        //// instruction decoding ////
        opcode.assign(instr_code[6, 0])
        alu_unsigned.assign(0)

        rs0_addr.assign(instr_code[19, 15])
        rs1_addr.assign(instr_code[24, 20])
        rd_addr.assign(instr_code[11, 7])

        funct3.assign(instr_code[14, 12])
        funct7.assign(instr_code[31, 25])
        shamt.assign(instr_code[24, 20])
        pred.assign(instr_code[27, 24])
        succ.assign(instr_code[23, 20])
        csrnum.assign(instr_code[31, 20])
        zimm.assign(instr_code[19, 15])

        immediate_I.assign(signext(instr_code[31, 20], 32))

        var immediate_S_src = ArrayList<hw_param>()
        immediate_S_src.add(instr_code[31, 25])
        immediate_S_src.add(instr_code[11, 7])
        immediate_S.assign(signext(cnct(immediate_S_src), 32))

        var immediate_B_src = ArrayList<hw_param>()
        immediate_B_src.add(instr_code[31])
        immediate_B_src.add(instr_code[7])
        immediate_B_src.add(instr_code[30, 25])
        immediate_B_src.add(instr_code[11, 8])
        immediate_B_src.add(hw_imm(1, "0"))
        immediate_B.assign(signext(cnct(immediate_B_src), 32))

        var immediate_U_src = ArrayList<hw_param>()
        immediate_U_src.add(instr_code[31, 12])
        immediate_U_src.add(hw_imm(12, "0"))
        immediate_U.assign(cnct(immediate_U_src))

        var immediate_J_src = ArrayList<hw_param>()
        immediate_J_src.add(instr_code[31])
        immediate_J_src.add(instr_code[19, 12])
        immediate_J_src.add(instr_code[20])
        immediate_J_src.add(instr_code[30, 21])
        immediate_J_src.add(hw_imm(1, "0"))
        immediate_J.assign(signext(cnct(immediate_J_src), 32))

        begcase(opcode)
        run {
            begbranch(opcode_LUI)
            run {
                op0_source.assign(OP0_SRC_IMM)
                rd_req.assign(1)
                rd_source.assign(RD_LUI)
                immediate.assign(immediate_U)
            }
            endbranch()

            begbranch(opcode_AUIPC)
            run {
                op0_source.assign(OP0_SRC_PC)
                op1_source.assign(OP1_SRC_IMM)
                alu_req.assign(1)
                alu_opcode.assign(aluop_ADD)
                rd_req.assign(1)
                rd_source.assign(RD_ALU)
                immediate.assign(immediate_U)
            }; endbranch()

            begbranch(opcode_JAL)
            run {
                op0_source.assign(OP0_SRC_PC)
                op1_source.assign(OP1_SRC_IMM)
                alu_req.assign(1)
                alu_opcode.assign(aluop_ADD)
                rd_req.assign(1)
                rd_source.assign(RD_PC_INC)
                jump_req.assign(1)
                jump_src.assign(JMP_SRC_ALU)
                immediate.assign(immediate_J)
            }; endbranch()

            begbranch(opcode_JALR)
            run {
                rs0_req.assign(1)
                op0_source.assign(OP0_SRC_RS1)
                op1_source.assign(OP1_SRC_IMM)
                alu_req.assign(1)
                alu_opcode.assign(aluop_ADD)
                rd_req.assign(1)
                rd_source.assign(RD_PC_INC)
                jump_req.assign(1)
                jump_src.assign(JMP_SRC_ALU)
                immediate.assign(immediate_I)
            }; endbranch()

            begbranch(opcode_BRANCH)
            run {
                rs0_req.assign(1)
                rs1_req.assign(1)
                alu_req.assign(1)
                alu_opcode.assign(aluop_SUB)
                jump_req_cond.assign(1)
                jump_src.assign(JMP_SRC_ALU)
                immediate.assign(immediate_B)

                begif(bor(eq2(funct3, 0x6), eq2(funct3, 0x7)))
                run {
                    alu_unsigned.assign(1)
                }; endif()
            }; endbranch()

            begbranch(opcode_LOAD)
            run {
                rs0_req.assign(1)
                op0_source.assign(OP0_SRC_RS1)
                op1_source.assign(OP1_SRC_IMM)
                rd_req.assign(1)
                rd_source.assign(RD_MEM)
                alu_req.assign(1)
                mem_req.assign(1)
                mem_cmd.assign(0)
                immediate.assign(immediate_I)
            }; endbranch()

            begbranch(opcode_STORE)
            run {
                rs0_req.assign(1)
                rs1_req.assign(1)
                op0_source.assign(OP0_SRC_RS1)
                op1_source.assign(OP1_SRC_IMM)
                alu_req.assign(1)
                mem_req.assign(1)
                mem_cmd.assign(1)
                immediate.assign(immediate_S)
            }; endbranch()

            begbranch(opcode_OP_IMM)
            run {
                rs0_req.assign(1)
                op0_source.assign(OP0_SRC_RS1)
                op1_source.assign(OP1_SRC_IMM)
                rd_req.assign(1)
                immediate.assign(immediate_I)
                alu_req.assign(1)

                begcase(funct3)
                run {
                    // ADDI
                    begbranch(0x0)
                    run {
                        alu_opcode.assign(aluop_ADD)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SLLI
                    begbranch(0x1)
                    run {
                        alu_opcode.assign(aluop_SLL)
                        rd_source.assign(RD_ALU)
                        immediate.assign(zeroext(instr_code[24, 20], 32))
                    }; endbranch()

                    // SLTI
                    begbranch(0x2)
                    run {
                        alu_opcode.assign(aluop_SLT)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // SLTIU
                    begbranch(0x3)
                    run {
                        alu_opcode.assign(aluop_SLT)
                        alu_unsigned.assign(1)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // XORI
                    begbranch(0x4)
                    run {
                        alu_opcode.assign(aluop_XOR)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SRLI, SRAI
                    begbranch(0x5)
                    run {
                        // SRAI
                        begif(instr_code[30])
                        run {
                            alu_opcode.assign(aluop_SRA)
                        }; endif()

                        // SRLI
                        begelse()
                        run {
                            alu_opcode.assign(aluop_SRL)
                        }; endif()

                        rd_source.assign(RD_ALU)
                        immediate.assign(zeroext(instr_code[24, 20], 32))
                    }; endbranch()

                    // ORI
                    begbranch(0x6)
                    run {
                        alu_opcode.assign(aluop_OR)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // ANDI
                    begbranch(0x7)
                    run {
                        alu_opcode.assign(aluop_AND)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                }; endcase()
            }; endbranch()

            begbranch(opcode_OP)
            run {
                rs0_req.assign(1)
                rs1_req.assign(1)
                op0_source.assign(OP0_SRC_RS1)
                op1_source.assign(OP1_SRC_RS2)
                rd_req.assign(1)
                rd_source.assign(RD_ALU)
                alu_req.assign(1)

                begcase(funct3)
                run {
                    // ADD/SUB
                    begbranch(0x0)
                    run {
                        // SUB
                        begif(instr_code[30])
                        run {
                            alu_opcode.assign(aluop_SUB)
                        }; endif()

                        // ADD
                        begelse()
                        run {
                            alu_opcode.assign(aluop_ADD)
                        }; endif()
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SLL
                    begbranch(0x1)
                    run {
                        alu_opcode.assign(aluop_SLL)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SLT
                    begbranch(0x2)
                    run {
                        alu_opcode.assign(aluop_SLT)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // SLTU
                    begbranch(0x3)
                    run {
                        alu_opcode.assign(aluop_SLT)
                        alu_unsigned.assign(1)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // XORI
                    begbranch(0x4)
                    run {
                        alu_opcode.assign(aluop_XOR)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SRL/SRA
                    begbranch(0x5)
                    run {
                        // SRA
                        begif(instr_code[30])
                        run {
                            alu_opcode.assign(aluop_SRA)
                        }; endif()
                        // SRL
                        begelse()
                        run {
                            alu_opcode.assign(aluop_SRL)
                        }; endif()
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // OR
                    begbranch(0x6)
                    run {
                        alu_opcode.assign(aluop_OR)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // AND
                    begbranch(0x7)
                    run {
                        alu_opcode.assign(aluop_AND)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                }; endcase()
            }; endbranch()

            begbranch(opcode_MISC_MEM)
            run {
                fencereq.assign(1)
            }; endbranch()

            begbranch(opcode_SYSTEM)
            run {
                begcase(funct3)
                run {
                    // EBREAK/ECALL
                    begbranch(0x0)
                    run {
                        // EBREAK
                        begif(instr_code[20])
                        run {
                            ebreakreq.assign(1)
                        }; endif()
                        // ECALL
                        begelse()
                        run {
                            ecallreq.assign(1)
                        }; endif()
                    }; endbranch()

                    //CSRRW
                    begbranch(0x1)
                    run {
                        csrreq.assign(1)
                        rs0_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        op0_source.assign(OP0_SRC_RS1)
                        op1_source.assign(OP1_SRC_CSR)
                    }; endbranch()

                    // CSRRS
                    begbranch(0x2)
                    run {
                        csrreq.assign(1)
                        rs0_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_OR)
                        op0_source.assign(OP0_SRC_RS1)
                        op1_source.assign(OP1_SRC_CSR)
                    }; endbranch()

                    // CSRRC
                    begbranch(0x3)
                    run {
                        csrreq.assign(1)
                        rs0_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_CLRB)
                        op0_source.assign(OP0_SRC_RS1)
                        op1_source.assign(OP1_SRC_CSR)
                    }; endbranch()

                    // CSRRWI
                    begbranch(0x5)
                    run {
                        csrreq.assign(1)
                        rd_req.assign(1)
                        op0_source.assign(OP0_SRC_IMM)
                        op1_source.assign(OP1_SRC_CSR)
                        immediate.assign(zeroext(zimm, 32))
                    }; endbranch()

                    // CSRRSI
                    begbranch(0x6)
                    run {
                        csrreq.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_CLRB)
                        op0_source.assign(OP0_SRC_IMM)
                        op1_source.assign(OP1_SRC_CSR)
                        immediate.assign(zeroext(zimm, 32))
                    }; endbranch()

                    // CSRRSI
                    begbranch(0x7)
                    run {
                        csrreq.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_CLRB)
                        op0_source.assign(OP0_SRC_IMM)
                        op1_source.assign(OP1_SRC_CSR)
                        immediate.assign(zeroext(zimm, 32))
                    }; endbranch()
                }; endcase()
            }; endbranch()

        }; endcase()

        curinstraddr_imm.assign(curinstr_addr + immediate)

        begif(mem_req)
        run {
            begcase(funct3)
            run {
                begbranch(0x0)
                run {
                    mem_be.assign(0x1)
                    load_signext.assign(1)
                }; endbranch()

                begbranch(0x1)
                run {
                    mem_be.assign(0x3)
                    load_signext.assign(1)
                }; endbranch()

                begbranch(0x2)
                run {
                    mem_be.assign(0xf)
                }; endbranch()

                begbranch(0x4)
                run {
                    mem_be.assign(0x1)
                }; endbranch()

                begbranch(0x5)
                run {
                    mem_be.assign(0x3)
                }; endbranch()
            }; endcase()
        }; endif()

        begif(eq2(instr_code, instrcode_MRET))
        run {
            mret_req.assign(1)
            jump_req.assign(1)
            jump_req_cond.assign(0)
            jump_src.assign(JMP_SRC_IMM)
            immediate.assign(MRETADDR)
        }; endif()

        begif(eq2(rd_addr, 0))
        run {
            rd_req.assign(0)
        }; endif()

    }
}

data class Exu_CFG(val ExecUnit : Exu,
                   val exu_num : Int,
                   val iq_length : Int,
                   val pref_impl : STREAM_PREF_IMPL)

open class MultiExu(val name : String, val MultiExu_CFG : Reordex_CFG, val io_iq_size : Int) {

    var ExecUnits  = mutableMapOf<String, Exu_CFG>()

    fun add_exu(exu : Exu, exu_num: Int, iq_length: Int, pref_impl : STREAM_PREF_IMPL) {
        if (ExecUnits.put(exu.name, Exu_CFG(exu, exu_num, iq_length, pref_impl)) != null) {
            ERROR("Exu addition error!")
        }
    }

    var RISCDecode = RISCDecodeLogic(MultiExu_CFG)

    fun reconstruct_expression(DEBUG_FLAG : Boolean,
                               cyclix_gen : hw_astc,
                               expr : hw_exec,
                               context : import_expr_context) {

        cyclix_gen as cyclix.Streaming

        cyclix_gen.import_expr(DEBUG_FLAG, expr, context, ::reconstruct_expression)
    }

    fun translate_to_cyclix(DEBUG_FLAG : Boolean) : cyclix.Generic {

        NEWLINE()
        MSG("################################################")
        MSG("#### Starting Reordex-to-Cyclix translation ####")
        MSG("#### module: " + name)
        MSG("################################################")

        var cyclix_gen = cyclix.Generic(name)

        MSG("generating control structures...")

        var CDB_NUM = 0
        var RRB_NUM = 0
        var EXU_NUM = 0
        var RISC_COMMIT_NUM = 0
        var RISC_LSU_NUM = 0
        var CDB_RISC_COMMIT_POS = 0
        var RRB_RISC_LSU_POS = 0

        for (ExUnit in ExecUnits) EXU_NUM += ExUnit.value.exu_num
        CDB_NUM = EXU_NUM
        RRB_NUM = EXU_NUM
        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            RISC_COMMIT_NUM = 1
            RISC_LSU_NUM = 1
            CDB_RISC_COMMIT_POS = EXU_NUM
            RRB_RISC_LSU_POS = EXU_NUM
            CDB_NUM += RISC_COMMIT_NUM
            RRB_NUM += RISC_LSU_NUM
        }

        var prf_dim = hw_dim_static()
        prf_dim.add(MultiExu_CFG.RF_width-1, 0)
        prf_dim.add(MultiExu_CFG.PRF_depth-1, 0)
        var PRF = cyclix_gen.uglobal("genPRF", prf_dim, "0")

        var PRF_mapped = cyclix_gen.uglobal("genPRF_mapped", MultiExu_CFG.PRF_depth-1, 0, hw_imm_ones(MultiExu_CFG.ARF_depth))

        var PRF_rdy = cyclix_gen.uglobal("genPRF_rdy", MultiExu_CFG.PRF_depth-1, 0, hw_imm_ones(MultiExu_CFG.PRF_depth))

        var arf_map_dim = hw_dim_static()
        arf_map_dim.add(MultiExu_CFG.PRF_addr_width-1, 0)
        arf_map_dim.add(MultiExu_CFG.ARF_depth-1, 0)

        var ARF_map_default = hw_imm_arr(arf_map_dim)
        for (RF_idx in 0 until MultiExu_CFG.PRF_depth) {
            if (RF_idx < MultiExu_CFG.ARF_depth) {
                ARF_map_default.AddSubImm(RF_idx.toString())
            } else {
                ARF_map_default.AddSubImm("0")
            }
        }

        var ARF_map = cyclix_gen.uglobal("genARF_map", arf_map_dim, ARF_map_default)        // ARF-to-PRF mappings

        var prf_src_dim = hw_dim_static()
        prf_src_dim.add(GetWidthToContain(CDB_NUM)-1, 0)
        prf_src_dim.add(MultiExu_CFG.PRF_depth-1, 0)
        var PRF_src = cyclix_gen.uglobal("genPRF_src", prf_src_dim, "0") // uncomputed PRF sources

        var exu_descrs = mutableMapOf<String, __exu_descr>()
        var exu_rst = cyclix_gen.ulocal("genexu_rst", 0, 0, "0")
        var global_structures = __global_structures(cyclix_gen, MultiExu_CFG, PRF, PRF_mapped, PRF_rdy, ARF_map, ARF_map_default, PRF_src, ExecUnits, exu_descrs, exu_rst)

        MSG("generating control structures: done")

        MSG("generating internal structures...")

        var cdb_struct  = hw_struct("cdb_struct")
        cdb_struct.addu("enb", 0, 0, "0")
        cdb_struct.add("data", MultiExu_CFG.resp_struct)
        var cdb = cyclix_gen.local("gencdb", cdb_struct, hw_dim_static(CDB_NUM-1, 0))       // Common Data Bus
        var rrb = cyclix_gen.local("genrrb", cdb_struct, hw_dim_static(RRB_NUM-1, 0))       // Return to ROB Bus
        var io_cdb_buf  =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) DUMMY_VAR
            else cyclix_gen.global("io_cdb_buf", cdb_struct)
        var io_cdb_rs1_wdata_buf =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) DUMMY_VAR
            else cyclix_gen.uglobal("io_cdb_rs1_wdata_buf", MultiExu_CFG.RF_width-1, 0, "0")

        var rob =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rob(cyclix_gen, "genrob", MultiExu_CFG.trx_inflight_num, MultiExu_CFG, RRB_NUM)
            else rob_risc(name, cyclix_gen, "genrob", MultiExu_CFG.trx_inflight_num, MultiExu_CFG, RRB_NUM)

        var TranslateInfo = __TranslateInfo()

        var IQ_insts = ArrayList<iq_buffer>()
        var ExUnits_insts = ArrayList<ArrayList<cyclix.hw_subproc>>()

        var exu_req     = cyclix_gen.local(cyclix_gen.GetGenName("exu_req"), MultiExu_CFG.req_struct)
        var exu_resp    = cyclix_gen.local(cyclix_gen.GetGenName("exu_resp"), MultiExu_CFG.resp_struct)

        var MRETADDR =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) DUMMY_VAR
            else cyclix_gen.uglobal("MRETADDR", 31, 0, "0")

        // CSRs
        var CSR_MCAUSE =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) DUMMY_VAR
            else cyclix_gen.uglobal("CSR_MCAUSE", 7, 0, "0")

        MSG("generating internal structures: done")

        var instr_fetch = (rob as hw_stage)
        var instr_req = hw_imm(0)
        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            instr_fetch = instr_fetch_buffer(name, cyclix_gen, "instr_fetch", 1, this, MultiExu_CFG, global_structures)
            instr_req = instr_req_stage(name, cyclix_gen, instr_fetch)
        }

        var ExUnit_idx = 0
        var fu_num = 0
        for (ExUnit in ExecUnits) {
            MSG("generating execution unit: " + ExUnit.value.ExecUnit.name + "...")

            var new_exu_descr = __exu_descr(mutableMapOf(), ArrayList(), ArrayList())

            for (ExUnit_num in 0 until ExUnit.value.exu_num) {
                var iq_buf = iq_buffer(cyclix_gen, ExUnit.key, ExUnit_num, "geniq_" + ExUnit.key + "_" + ExUnit_num, ExUnit.value.iq_length, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), true, fu_num, CDB_NUM)
                new_exu_descr.IQ_insts.add(iq_buf)
                IQ_insts.add(iq_buf)
            }

            MSG("generating submodules...")
            var exu_cyclix_gen = cyclix.Streaming("genexu_" + ExUnit.value.ExecUnit.name, MultiExu_CFG.req_struct, MultiExu_CFG.resp_struct, ExUnit.value.pref_impl)
            MSG("generating submodules: done")

            MSG("generating locals...")
            for (local in ExUnit.value.ExecUnit.locals)
                new_exu_descr.var_dict.put(local, exu_cyclix_gen.local(local.name, local.vartype, local.defimm))
            for (imm_num in 0 until ExUnit.value.ExecUnit.src_imms.size)
                new_exu_descr.var_dict.put(MultiExu_CFG.src_imms[imm_num], new_exu_descr.var_dict[ExUnit.value.ExecUnit.src_imms[imm_num]!!]!!)
            for (rs_num in 0 until ExUnit.value.ExecUnit.rss.size)
                new_exu_descr.var_dict.put(MultiExu_CFG.rss[rs_num], new_exu_descr.var_dict[ExUnit.value.ExecUnit.rss[rs_num]!!]!!)
            MSG("generating locals: done")

            MSG("generating globals...")
            for (global in ExUnit.value.ExecUnit.globals)
                new_exu_descr.var_dict.put(global, exu_cyclix_gen.global(global.name, global.vartype, global.defimm))
            MSG("generating globals: done")

            MSG("generating intermediates...")
            for (genvar in ExUnit.value.ExecUnit[0].genvars)
                new_exu_descr.var_dict.put(genvar, exu_cyclix_gen.local(genvar.name, genvar.vartype, genvar.defimm))
            MSG("generating intermediates: done")

            MSG("generating DstIms...")
            for (dst_imm in MultiExu_CFG.dst_imms)
                new_exu_descr.var_dict.put(dst_imm, TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict).GetFracRef(dst_imm.name))
            MSG("generating DstIms: done")

            MSG("generating logic...")

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict), exu_cyclix_gen.stream_req_var)

            for (imm_num in 0 until MultiExu_CFG.src_imms.size) {
                exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.src_imms[imm_num], new_exu_descr.var_dict), exu_cyclix_gen.subStruct((TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict)), MultiExu_CFG.src_imms[imm_num].name))
            }
            for (rs_num in 0 until MultiExu_CFG.rss.size) {
                exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.rss[rs_num], new_exu_descr.var_dict), exu_cyclix_gen.subStruct((TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict)), "rs" + rs_num + "_rdata"))
            }

            for (expr in ExUnit.value.ExecUnit[0].expressions) {
                reconstruct_expression(false,
                    exu_cyclix_gen,
                    expr,
                    import_expr_context(new_exu_descr.var_dict))
            }

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict).GetFracRef("wdata"), TranslateVar(ExUnit.value.ExecUnit.rd0, new_exu_descr.var_dict) )

            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var, TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict))
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var.GetFracRef("tag"), exu_cyclix_gen.stream_req_var.GetFracRef("rd0_tag"))
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var.GetFracRef("trx_id"), exu_cyclix_gen.stream_req_var.GetFracRef("trx_id"))

            exu_cyclix_gen.end()

            MSG("generating logic: done")

            MSG("generating submodule instances...")
            var ExUnit_insts = ArrayList<cyclix.hw_subproc>()
            for (exu_num in 0 until ExUnit.value.exu_num) {
                var exu_inst = cyclix_gen.subproc(exu_cyclix_gen.name + "_" + exu_num, exu_cyclix_gen)
                exu_inst.AddResetDriver(exu_rst)
                ExUnit_insts.add(exu_inst)
            }
            ExUnits_insts.add(ExUnit_insts)

            var exu_info = __exu_info(
                exu_cyclix_gen,
                exu_req,
                exu_resp
            )

            TranslateInfo.exu_assocs.put(ExUnit.value.ExecUnit, exu_info)
            MSG("generating submodule instances: done")

            for (rs_num in 0 until ExUnit.value.ExecUnit.rss.size)
                if (new_exu_descr.var_dict[ExUnit.value.ExecUnit.rss[rs_num]!!]!!.read_done) {
                    MSG("Exu waits for: " + ExUnit.value.ExecUnit.rss[rs_num]!!.name)
                    new_exu_descr.rs_use_flags.add(true)
                } else {
                    new_exu_descr.rs_use_flags.add(false)
                }
            exu_descrs.put(ExUnit.key, new_exu_descr)

            ExUnit_idx++
            fu_num += ExUnit.value.exu_num

            MSG("generating execution unit " + ExUnit.value.ExecUnit.name + ": done")
        }

        MSG("generating I/O IQ...")
        var io_iq =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) iq_buffer(cyclix_gen, "genstore", 0, "genstore", io_iq_size, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), false, fu_num, CDB_NUM)
            else iq_buffer(cyclix_gen, "genlsu", 0, "genlsu", io_iq_size, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), false, RRB_RISC_LSU_POS, CDB_NUM)
        IQ_insts.add(io_iq)
        MSG("generating I/O IQ: done")

        MSG("generating logic...")

        cyclix_gen.MSG_COMMENT("Initializing CDB...")

        // EXU
        var exu_cdb_num = 0
        for (exu_num in 0 until ExUnits_insts.size) {
            for (exu_inst_num in 0 until ExUnits_insts[exu_num].size) {
                var exu_cdb_inst        = cdb.GetFracRef(exu_cdb_num)
                var exu_cdb_inst_enb    = exu_cdb_inst.GetFracRef("enb")
                var exu_cdb_inst_data   = exu_cdb_inst.GetFracRef("data")

                cyclix_gen.assign(exu_cdb_inst_enb, cyclix_gen.fifo_internal_rd_unblk(ExUnits_insts[exu_num][exu_inst_num], cyclix.STREAM_RESP_BUS_NAME, exu_cdb_inst_data))
                exu_cdb_num++
            }
        }
        for (exu_num in 0 until EXU_NUM) {
            cyclix_gen.assign(rrb.GetFracRef(exu_num), cdb.GetFracRef(exu_num))
        }

        // RISC LSU
        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            cyclix_gen.assign(rrb.GetFracRef(RRB_RISC_LSU_POS), io_cdb_buf)
        }

        cyclix_gen.MSG_COMMENT("Initializing CDB: done")

        var renamed_uop_buf =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rename_buffer(cyclix_gen, "genrenamed_uop_buf", 1, MultiExu_CFG, ExecUnits.size, CDB_NUM)
            else rename_buffer_risc(cyclix_gen, "genrenamed_uop_buf", 1, MultiExu_CFG, ExecUnits.size, CDB_NUM)

        cyclix_gen.MSG_COMMENT("ROB committing...")
        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rob.Commit(global_structures)
        else {
            var bufs_to_reset = ArrayList<hw_stage>()
            bufs_to_reset.add(rob)
            for (IQ_inst in IQ_insts) bufs_to_reset.add(IQ_inst)
            bufs_to_reset.add(renamed_uop_buf)
            bufs_to_reset.add(instr_fetch)
            (rob as rob_risc).Commit(global_structures, (instr_req as instr_req_stage).pc, bufs_to_reset, cdb.GetFracRef(CDB_RISC_COMMIT_POS), MRETADDR, CSR_MCAUSE)
        }
        cyclix_gen.MSG_COMMENT("ROB committing: done")

        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            cyclix_gen.MSG_COMMENT("Deactivating CDB in case of backoff...")
            cyclix_gen.begif(global_structures.exu_rst)
            run {
                for (cdb_idx in 0 until cdb.GetWidth()) {
                    cyclix_gen.assign(cdb.GetFracRef(cdb_idx).GetFracRef("enb"), 0)
                }
            }; cyclix_gen.endif()
            cyclix_gen.MSG_COMMENT("Deactivating CDB in case of backoff: done")
        }

        rob.FillFromRRB(MultiExu_CFG, rrb, io_cdb_rs1_wdata_buf)

        io_iq.ProcessIO(io_cdb_buf, io_cdb_rs1_wdata_buf)

        var fu_id = 0
        for (ExUnit in ExecUnits) {
            for (ExUnit_num in 0 until ExUnit.value.exu_num) {

                cyclix_gen.MSG_COMMENT("IQ processing: ExUnit: " + ExUnit.key + ", instance num: " + ExUnit_num)

                var IQ_inst = exu_descrs[ExUnit.key]!!.IQ_insts[ExUnit_num]
                IQ_inst.preinit_ctrls()
                IQ_inst.init_locals()

                IQ_inst.Issue(ExUnit.value, exu_req, ExUnits_insts[fu_id][ExUnit_num], ExUnit_num)

            }
            fu_id++
        }

        renamed_uop_buf.preinit_ctrls()
        renamed_uop_buf.init_locals()

        cyclix_gen.MSG_COMMENT("broadcasting FU results to IQ and renamed buffer...")
        for (cdb_idx in 0 until CDB_NUM) {

            var exu_cdb_inst        = cdb.GetFracRef(cdb_idx)
            var exu_cdb_inst_enb    = exu_cdb_inst.GetFracRef("enb")
            var exu_cdb_inst_data   = exu_cdb_inst.GetFracRef("data")
            var exu_cdb_inst_tag    = exu_cdb_inst_data.GetFracRef("tag")
            var exu_cdb_inst_wdata  = exu_cdb_inst_data.GetFracRef("wdata")

            cyclix_gen.begif(exu_cdb_inst_enb)
            run {

                global_structures.WritePRF(exu_cdb_inst_tag, exu_cdb_inst_wdata)

                // broadcasting FU results to renamed buffer
                for (renamed_uop_buf_idx in 0 until renamed_uop_buf.TRX_BUF_SIZE) {
                    var renamed_uop_buf_entry = renamed_uop_buf.TRX_BUF.GetFracRef(renamed_uop_buf_idx)
                    for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {

                        var rs_rdy      = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_rdy")
                        var rs_tag      = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_tag")
                        var rs_rdata    = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_rdata")

                        cyclix_gen.begif(!rs_rdy)
                        run {
                            cyclix_gen.begif(cyclix_gen.eq2(rs_tag, exu_cdb_inst_tag))
                            run {
                                // setting IQ entry ready
                                cyclix_gen.assign(rs_rdata, exu_cdb_inst_wdata)
                                cyclix_gen.assign(rs_rdy, 1)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()
                    }

                    //// setting rdy for io_req if data generated ////
                    cyclix_gen.begif(renamed_uop_buf_entry.GetFracRef("io_req"))
                    run {
                        cyclix_gen.assign(renamed_uop_buf_entry.GetFracRef("rdy"), renamed_uop_buf_entry.GetFracRef("rs0_rdy"))
                    }; cyclix_gen.endif()
                }

            }; cyclix_gen.endif()
        }

        // broadcasting FU results to IQ
        for (IQ_inst in IQ_insts) IQ_inst.FillFromCDB(cdb)

        cyclix_gen.MSG_COMMENT("broadcasting FU results to IQ and renamed buffer: done")

        renamed_uop_buf.Process(rob, PRF_src, io_iq, ExecUnits, IQ_insts, CDB_RISC_COMMIT_POS)

        cyclix_gen.MSG_COMMENT("renaming...")

        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) {
            var frontend = coproc_frontend(name, cyclix_gen, MultiExu_CFG, global_structures)
            frontend.Send_toRenameBuf(renamed_uop_buf)

        } else {            // MultiExu_CFG.mode == REORDEX_MODE.RISC
            (instr_fetch as instr_fetch_buffer).Process(renamed_uop_buf, MRETADDR, CSR_MCAUSE)
            (instr_req as instr_req_stage).Process(instr_fetch)
        }

        cyclix_gen.MSG_COMMENT("renaming: done")

        cyclix_gen.end()

        MSG("generating logic: done")

        MSG("#################################################")
        MSG("#### Reordex-to-Cyclix translation complete! ####")
        MSG("#### module: " + name)
        MSG("#################################################")

        return cyclix_gen
    }
}
