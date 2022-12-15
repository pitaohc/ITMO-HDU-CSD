############################################################
## This file is generated automatically by Vivado HLS.
## Please DO NOT edit it.
## Copyright (C) 1986-2019 Xilinx, Inc. All Rights Reserved.
############################################################
open_project Cubic
set_top Cubic
add_files Cubic/srcs/Cubic.c
add_files -tb Cubic/srcs/main.c -cflags "-Wno-unknown-pragmas" -csimflags "-Wno-unknown-pragmas"
open_solution "Cubic"
set_part {xc7a100t-csg324-1} -tool vivado
create_clock -period 10 -name default
#source "./Cubic/Cubic/directives.tcl"
csim_design -clean
csynth_design
cosim_design
export_design -format ip_catalog
