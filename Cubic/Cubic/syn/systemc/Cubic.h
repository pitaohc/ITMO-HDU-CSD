// ==============================================================
// RTL generated by Vivado(TM) HLS - High-Level Synthesis from C, C++ and SystemC
// Version: 2019.1
// Copyright (C) 1986-2019 Xilinx, Inc. All Rights Reserved.
// 
// ===========================================================

#ifndef _Cubic_HH_
#define _Cubic_HH_

#include "systemc.h"
#include "AESL_pkg.h"


namespace ap_rtl {

struct Cubic : public sc_module {
    // Port declarations 8
    sc_in_clk ap_clk;
    sc_in< sc_logic > ap_rst;
    sc_in< sc_logic > ap_start;
    sc_out< sc_logic > ap_done;
    sc_out< sc_logic > ap_idle;
    sc_out< sc_logic > ap_ready;
    sc_in< sc_lv<32> > x;
    sc_out< sc_lv<32> > ap_return;


    // Module declarations
    Cubic(sc_module_name name);
    SC_HAS_PROCESS(Cubic);

    ~Cubic();

    sc_trace_file* mVcdFile;

    ofstream mHdltvinHandle;
    ofstream mHdltvoutHandle;
    sc_signal< sc_lv<5> > ap_CS_fsm;
    sc_signal< sc_logic > ap_CS_fsm_state1;
    sc_signal< sc_lv<32> > sext_ln29_fu_75_p1;
    sc_signal< sc_lv<32> > sext_ln29_reg_162;
    sc_signal< sc_logic > ap_CS_fsm_state2;
    sc_signal< sc_lv<32> > y_fu_87_p2;
    sc_signal< sc_lv<32> > y_reg_170;
    sc_signal< sc_lv<1> > tmp_fu_79_p3;
    sc_signal< sc_lv<32> > sub_ln32_fu_99_p2;
    sc_signal< sc_lv<32> > sub_ln32_reg_176;
    sc_signal< sc_lv<6> > s_fu_105_p2;
    sc_signal< sc_lv<6> > s_reg_181;
    sc_signal< sc_lv<32> > y_1_fu_111_p2;
    sc_signal< sc_lv<32> > y_1_reg_186;
    sc_signal< sc_logic > ap_CS_fsm_state3;
    sc_signal< sc_lv<32> > mul_ln32_fu_116_p2;
    sc_signal< sc_lv<32> > mul_ln32_reg_191;
    sc_signal< sc_lv<32> > y_2_fu_143_p3;
    sc_signal< sc_logic > ap_CS_fsm_state4;
    sc_signal< sc_lv<32> > select_ln33_fu_149_p3;
    sc_signal< sc_lv<6> > s_0_reg_42;
    sc_signal< sc_lv<32> > y_0_reg_53;
    sc_signal< sc_lv<32> > p_0_reg_65;
    sc_signal< sc_lv<32> > shl_ln32_fu_93_p2;
    sc_signal< sc_lv<32> > or_ln32_1_fu_121_p2;
    sc_signal< sc_lv<32> > b_fu_126_p2;
    sc_signal< sc_lv<1> > icmp_ln33_fu_131_p2;
    sc_signal< sc_lv<32> > sub_ln35_fu_137_p2;
    sc_signal< sc_logic > ap_CS_fsm_state5;
    sc_signal< sc_lv<5> > ap_NS_fsm;
    static const sc_logic ap_const_logic_1;
    static const sc_logic ap_const_logic_0;
    static const sc_lv<5> ap_ST_fsm_state1;
    static const sc_lv<5> ap_ST_fsm_state2;
    static const sc_lv<5> ap_ST_fsm_state3;
    static const sc_lv<5> ap_ST_fsm_state4;
    static const sc_lv<5> ap_ST_fsm_state5;
    static const sc_lv<32> ap_const_lv32_0;
    static const sc_lv<32> ap_const_lv32_1;
    static const sc_lv<1> ap_const_lv1_0;
    static const sc_lv<32> ap_const_lv32_2;
    static const sc_lv<32> ap_const_lv32_3;
    static const sc_lv<6> ap_const_lv6_1E;
    static const sc_lv<32> ap_const_lv32_5;
    static const sc_lv<6> ap_const_lv6_3D;
    static const sc_lv<1> ap_const_lv1_1;
    static const sc_lv<32> ap_const_lv32_4;
    static const bool ap_const_boolean_1;
    // Thread declarations
    void thread_ap_clk_no_reset_();
    void thread_ap_CS_fsm_state1();
    void thread_ap_CS_fsm_state2();
    void thread_ap_CS_fsm_state3();
    void thread_ap_CS_fsm_state4();
    void thread_ap_CS_fsm_state5();
    void thread_ap_done();
    void thread_ap_idle();
    void thread_ap_ready();
    void thread_b_fu_126_p2();
    void thread_icmp_ln33_fu_131_p2();
    void thread_mul_ln32_fu_116_p2();
    void thread_or_ln32_1_fu_121_p2();
    void thread_s_fu_105_p2();
    void thread_select_ln33_fu_149_p3();
    void thread_sext_ln29_fu_75_p1();
    void thread_shl_ln32_fu_93_p2();
    void thread_sub_ln32_fu_99_p2();
    void thread_sub_ln35_fu_137_p2();
    void thread_tmp_fu_79_p3();
    void thread_y_1_fu_111_p2();
    void thread_y_2_fu_143_p3();
    void thread_y_fu_87_p2();
    void thread_ap_NS_fsm();
    void thread_hdltv_gen();
};

}

using namespace ap_rtl;

#endif