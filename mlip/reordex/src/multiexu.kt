/*
 * multiexu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

data class MultiExu_CFG_RF(val input_RF_width : Int,
                           val input_RF_depth : Int,
                           val rename_RF: Boolean,
                           val rename_RF_depth : Int)

open class MultiExu(name_in : String, MultiExu_cfg_rf_in : MultiExu_CFG_RF, rob_size_in : Int) : hw_astc_stdif() {

    val name = name_in
    val MultiExu_cfg_rf = MultiExu_cfg_rf_in
    val input_rf_addr_width = GetWidthToContain(MultiExu_cfg_rf.input_RF_depth)
    val rename_rf_addr_width = GetWidthToContain(MultiExu_cfg_rf.rename_RF_depth)
    val rob_size = rob_size_in

    override var GenNamePrefix   = "reordex"

    var locals          = ArrayList<hw_var>()
    var globals         = ArrayList<hw_global>()

    var ExecUnits  = mutableMapOf<String, hw_exec_unit>()

    fun add_exu(name_in : String, exu_num_in: Int, stage_num_in: Int) : hw_exec_unit {
        if (FROZEN_FLAG) ERROR("Failed to add stage " + name_in + ": ASTC frozen")
        var new_exec_unit = hw_exec_unit(name_in, exu_num_in, stage_num_in, this)
        if (ExecUnits.put(new_exec_unit.name, new_exec_unit) != null) {
            ERROR("Stage addition problem!")
        }
        return new_exec_unit
    }

    fun begexu(exu : hw_exec_unit) {
        if (FROZEN_FLAG) ERROR("Failed to begin stage " + exu.name + ": ASTC frozen")
        if (this.size != 0) ERROR("reordex ASTC inconsistent!")
        // TODO: validate stage presence
        add(exu)
    }

    fun endexu() {
        if (FROZEN_FLAG) ERROR("Failed to end stage: ASTC frozen")
        if (this.size != 1) ERROR("Stage ASTC inconsistent!")
        if (this[0].opcode != OP_STAGE) ERROR("Stage ASTC inconsistent!")
        this.clear()
    }

    private fun add_local(new_local: hw_local) {
        if (FROZEN_FLAG) ERROR("Failed to add local " + new_local.name + ": ASTC frozen")

        if (wrvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)
        if (rdvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)

        wrvars.put(new_local.name, new_local)
        rdvars.put(new_local.name, new_local)
        locals.add(new_local)
        new_local.default_astc = this
    }

    fun local(name: String, vartype : hw_type, defval: String): hw_local {
        var ret_var = hw_local(name, vartype, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_local {
        var ret_var = hw_local(name, hw_type(src_struct_in, dimensions), "0")
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct): hw_local {
        var ret_var = hw_local(name, hw_type(src_struct_in), "0")
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    private fun add_local_sticky(new_local_sticky: hw_local_sticky) {
        if (FROZEN_FLAG) ERROR("Failed to add local_sticky " + new_local_sticky.name + ": ASTC frozen")

        if (wrvars.containsKey(new_local_sticky.name)) ERROR("Naming conflict for local_sticky: " + new_local_sticky.name)
        if (rdvars.containsKey(new_local_sticky.name)) ERROR("Naming conflict for local_sticky: " + new_local_sticky.name)

        wrvars.put(new_local_sticky.name, new_local_sticky)
        rdvars.put(new_local_sticky.name, new_local_sticky)
        locals.add(new_local_sticky)
        new_local_sticky.default_astc = this
    }

    fun local_sticky(name: String, vartype: hw_type, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, vartype, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(src_struct_in, dimensions), "0")
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, src_struct_in: hw_struct): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(src_struct_in), "0")
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    private fun add_global(new_global: hw_global) {
        if (FROZEN_FLAG) ERROR("Failed to add global " + new_global.name + ": ASTC frozen")

        if (wrvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)
        if (rdvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)

        wrvars.put(new_global.name, new_global)
        rdvars.put(new_global.name, new_global)
        globals.add(new_global)
        new_global.default_astc = this
    }

    fun global(name: String, vartype: hw_type, defval: String): hw_global {
        var ret_var = hw_global(name, vartype, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_global {
        var ret_var = hw_global(name, hw_type(src_struct_in, dimensions), "0")
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct): hw_global {
        var ret_var = hw_global(name, hw_type(src_struct_in), "0")
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun translate_to_cyclix(DEBUG_FLAG : Boolean) : cyclix.module {

        MSG("Translating to cyclix: beginning")

        var cyclix_gen = cyclix.module(name)

        //// Generating interfaces ////
        // cmd (sequential instruction stream) //
        var cmd_req_struct = cyclix_gen.add_struct(name + "_cmd_req_struct")
        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    input_rf_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(ExecUnits.size)-1, 0, "0")
        cmd_req_struct.addu("fu_rs0",    input_rf_addr_width-1, 0, "0")
        cmd_req_struct.addu("fu_rs1",    input_rf_addr_width-1, 0, "0")
        cmd_req_struct.addu("fu_rd",    input_rf_addr_width-1, 0, "0")
        var cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))
        var cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(MultiExu_cfg_rf.input_RF_width-1, 0)))

        // TODO: memory interface?

        var MAX_INSTR_NUM = MultiExu_cfg_rf.input_RF_depth + rob_size
        for (ExecUnit in ExecUnits) {
            MAX_INSTR_NUM += ExecUnit.value.exu_num * ExecUnit.value.stage_num
        }

        val TAG_WIDTH = GetWidthToContain(MAX_INSTR_NUM)

        var uop_struct = cyclix_gen.add_struct("uop_struct")
        uop_struct.addu("enb",     0, 0, "0")
        uop_struct.addu("opcode",     0, 0, "0")
        uop_struct.addu("rs0_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        uop_struct.addu("rs1_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        uop_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")
        uop_struct.addu("rd_wdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")

        var rob_struct = cyclix_gen.add_struct("rob_struct")
        rob_struct.addu("enb",     0, 0, "0")
        rob_struct.addu("sent",     0, 0, "0")
        rob_struct.addu("rdy",     0, 0, "0")
        rob_struct.addu("fu_id",     GetWidthToContain(ExecUnits.size)-1, 0, "0")
        rob_struct.addu("opcode",     0, 0, "0")
        rob_struct.addu("rs0_rdy",     0, 0, "0")
        rob_struct.addu("rs0_tag",     TAG_WIDTH-1, 0, "0")
        rob_struct.addu("rs0_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        rob_struct.addu("rs1_rdy",     0, 0, "0")
        rob_struct.addu("rs1_tag",     TAG_WIDTH-1, 0, "0")
        rob_struct.addu("rs1_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        rob_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")
        rob_struct.addu("rd_wdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")

        var req_struct = cyclix_gen.add_struct("req_struct")
        req_struct.addu("enb",     0, 0, "0")
        req_struct.addu("opcode",     0, 0, "0")
        req_struct.addu("rdy",     0, 0, "0")
        req_struct.addu("rs0_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        req_struct.addu("rs1_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        req_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")

        var resp_struct = cyclix_gen.add_struct("resp_struct")
        resp_struct.addu("enb",     0, 0, "0")
        resp_struct.addu("tag",     TAG_WIDTH-1, 0, "0")
        resp_struct.addu("wdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")

        var commit_struct = cyclix_gen.add_struct("commit_struct")
        commit_struct.addu("enb",     0, 0, "0")
        commit_struct.addu("rdy",     0, 0, "0")
        commit_struct.addu("rd_enb",     0, 0, "0")
        commit_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")
        commit_struct.addu("rd_wdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")

        var TranslateInfo = __TranslateInfo()

        var rob = cyclix_gen.global("genrob_" + name, rob_struct, rob_size-1, 0)
        for (ExUnit in ExecUnits) {

            var exu_vars = ArrayList<__exu_var_assoc>()
            var exu_opcode = cyclix_gen.global("genexu_" + ExUnit.value.exu_opcode.name, ExUnit.value.exu_opcode.vartype, ExUnit.value.exu_opcode.defval)
            exu_vars.add(__exu_var_assoc(ExUnit.value.exu_opcode, exu_opcode))
            var rs0_rdata = cyclix_gen.global("genexu_" + ExUnit.value.rs0_rdata.name, ExUnit.value.rs0_rdata.vartype, ExUnit.value.rs0_rdata.defval)
            exu_vars.add(__exu_var_assoc(ExUnit.value.rs0_rdata, rs0_rdata))
            var rs1_rdata = cyclix_gen.global("genexu_" + ExUnit.value.rs1_rdata.name, ExUnit.value.rs1_rdata.vartype, ExUnit.value.rs1_rdata.defval)
            exu_vars.add(__exu_var_assoc(ExUnit.value.rs1_rdata, rs1_rdata))
            var rd_wdata = cyclix_gen.global("genexu_" + ExUnit.value.rd_wdata.name, ExUnit.value.rd_wdata.vartype, ExUnit.value.rd_wdata.defval)
            exu_vars.add(__exu_var_assoc(ExUnit.value.rd_wdata, rd_wdata))

            var exu_info = __exu_info(
                cyclix_gen.global("genexu_" + ExUnit.value.name + "_req", req_struct, ExUnit.value.exu_num-1, 0),
                cyclix_gen.global("genexu_" + ExUnit.value.name + "_resp", resp_struct, ExUnit.value.exu_num-1, 0),
                exu_vars
            )

            TranslateInfo.exu_assocs.put(ExUnit.value, exu_info)
        }
        var commit_bus = cyclix_gen.global("genexu_" + name + "_commit", commit_struct)

        // committing ROB head
        var rob_head = cyclix_gen.indexed(rob, 0)
        cyclix_gen.begif(cyclix_gen.subStruct(cyclix_gen.indexed(rob, 0), "enb"))
        run {
            cyclix_gen.assign(
                commit_bus,
                hw_fracs(hw_frac_SubStruct("enb")),
                1)
            cyclix_gen.assign(
                commit_bus,
                hw_fracs(hw_frac_SubStruct("rd_enb")),
                1)
            cyclix_gen.assign(
                commit_bus,
                hw_fracs(hw_frac_SubStruct("rd_tag")),
                cyclix_gen.subStruct(rob_head, "rd_tag"))
            cyclix_gen.assign(
                commit_bus,
                hw_fracs(hw_frac_SubStruct("rd_wdata")),
                cyclix_gen.subStruct(rob_head, "rd_wdata"))

            cyclix_gen.begif(cyclix_gen.subStruct(commit_bus, "rdy"))
            run {

                // shifting ops
                var rob_shift_iter = cyclix_gen.begforrange(rob, hw_imm(0), hw_imm(rob.vartype.dimensions.last().msb-1))
                run {
                    cyclix_gen.assign(
                        rob,
                        hw_fracs(hw_frac_V(rob_shift_iter.iter_num)),
                        cyclix_gen.indexed(rob, rob_shift_iter.iter_num_next))
                }; cyclix_gen.endloop()
                cyclix_gen.assign(
                    rob,
                    hw_fracs(hw_frac_C(rob.vartype.dimensions.last().msb)),
                    0)
            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        // issuing operations from ROB to FUs
        MSG("Translating: issuing operations from ROB to FUs")
        var rob_iter = cyclix_gen.begforall(rob)
        run {
            cyclix_gen.begif(cyclix_gen.subStruct(rob_iter.iter_elem, "enb"))
            run {
                cyclix_gen.begif(cyclix_gen.band(cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdy"), cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdy")))
                run {
                    // asserting op to FU req bus
                    cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "sent"))
                    run {
                        var fu_id = 0
                        for (exu_assoc in TranslateInfo.exu_assocs) {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "fu_id"), fu_id))
                            run {
                                var req_bus_iter = cyclix_gen.begforall(exu_assoc.value.req_bus)
                                run {
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("enb")),
                                        1)
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("opcode")),
                                        cyclix_gen.subStruct(rob_iter.iter_elem, "opcode"))
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("rs0_rdata")),
                                        cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdata"))
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("rs1_rdata")),
                                        cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdata"))
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("rd_tag")),
                                        cyclix_gen.subStruct(rob_iter.iter_elem, "rd_tag"))

                                    cyclix_gen.begif(cyclix_gen.subStruct(req_bus_iter.iter_elem, "rdy"))
                                    run {
                                        cyclix_gen.assign(
                                            rob,
                                            hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("sent")),
                                            1)
                                    }; cyclix_gen.endif()
                                }; cyclix_gen.endloop()
                            }; cyclix_gen.endif()
                            fu_id++
                        }
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()
        }; cyclix_gen.endloop()

        // broadcasting FU results to ROB
        MSG("Translating: broadcasting FU results to ROB")
        for (exu_assoc in TranslateInfo.exu_assocs) {
            var resp_bus_iter = cyclix_gen.begforall(exu_assoc.value.resp_bus)
            run {
                cyclix_gen.begif(cyclix_gen.subStruct(resp_bus_iter.iter_elem, "enb"))
                run {
                    var rob_iter = cyclix_gen.begforall(rob)
                    run {

                        // reading rs0
                        cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdy"))
                        run {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_tag"), cyclix_gen.subStruct(resp_bus_iter.iter_elem, "tag")))
                            run {
                                // setting rs0 ROB entry ready
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs0_rdata")),
                                    cyclix_gen.subStruct(resp_bus_iter.iter_elem, "wdata"))
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs0_rdy")),
                                    1)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                        // reading rs1
                        cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdy"))
                        run {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_tag"), cyclix_gen.subStruct(resp_bus_iter.iter_elem, "tag")))
                            run {
                                // setting rs1 ROB entry ready
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs1_rdata")),
                                    cyclix_gen.subStruct(resp_bus_iter.iter_elem, "wdata"))
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs1_rdy")),
                                    1)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                        // reading rd
                        cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rdy"))
                        run {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rd_tag"), cyclix_gen.subStruct(resp_bus_iter.iter_elem, "tag")))
                            run {
                                // setting ROB entry ready for commit
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rd_wdata")),
                                    cyclix_gen.subStruct(resp_bus_iter.iter_elem, "wdata"))
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rdy")),
                                    1)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                    }; cyclix_gen.endloop()
                }; cyclix_gen.endif()
            }; cyclix_gen.endloop()
        }

        cyclix_gen.end()
        MSG(DEBUG_FLAG, "Translating to cyclix: complete")
        return cyclix_gen
    }
}