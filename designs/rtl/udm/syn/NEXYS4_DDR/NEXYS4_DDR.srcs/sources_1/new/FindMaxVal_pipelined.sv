`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2022/11/15 19:35:39
// Design Name: 
// Module Name: FindMaxVal_pipelined
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////
module FindMaxVal_pipelined (
    input clk_i
    , input rst_i
 
    , input [31:0] elem_bi [15:0]
 
    , output logic [31:0] max_elem_bo
    , output logic [3:0] max_index_bo
);
//// stage 0 ////
// intermediate signals declaration
logic [31:0] max_elem_stage0 [7:0];
logic [31:0] max_index_stage0 [7:0];
logic [31:0] max_elem_stage0_next [7:0];
logic [31:0] max_index_stage0_next [7:0];
// combinational logic
always @*
    begin
    for(integer i=0; i<8; i++)
        begin
        max_elem_stage0_next[i] = 0;
        max_index_stage0_next[i] = 0;
        if (elem_bi[(i<<1)] > elem_bi[(i<<1)+1])
            begin
            max_elem_stage0_next[i] = elem_bi[(i<<1)];
            max_index_stage0_next[i] = i<<1;
            end
        else
            begin
            max_elem_stage0_next[i] = elem_bi[(i<<1)+1];
            max_index_stage0_next[i] = (i<<1)+1;
            end
        end
    end

// writing to registers
always @(posedge clk_i)
    begin
    if (rst_i)
        begin
        for (integer i=0; i<8; i++) max_elem_stage0[i] <= 0;
        for (integer i=0; i<8; i++) max_index_stage0[i] <= 0;
        end
    else
        begin
        for (integer i=0; i<8; i++) max_elem_stage0[i] <= max_elem_stage0_next[i];
        for (integer i=0; i<8; i++) max_index_stage0[i] <= max_index_stage0_next[i];
        end
    end
//// stage 1 ////
// intermediate signals declaration
logic [31:0] max_elem_stage1 [3:0];
logic [31:0] max_index_stage1 [3:0];
logic [31:0] max_elem_stage1_next [3:0];
logic [31:0] max_index_stage1_next [3:0];
// combinational logic
always @*
    begin
    for(integer i=0; i<4; i++)
        begin
        max_elem_stage1_next[i] = 0;
        max_index_stage1_next[i] = 0;
        if (max_elem_stage0[(i<<1)] > max_elem_stage0[(i<<1)+1])
            begin
            max_elem_stage1_next[i] = max_elem_stage0[(i<<1)];
            max_index_stage1_next[i] = max_index_stage0[(i<<1)];
            end
        else
            begin
            max_elem_stage1_next[i] = max_elem_stage0[(i<<1)+1];
            max_index_stage1_next[i] = max_index_stage0[(i<<1)+1];
            end
        end
    end
// writing to registers
always @(posedge clk_i)
    begin
    if (rst_i)
        begin
        for (integer i=0; i<4; i++) max_elem_stage1[i] <= 0;
        for (integer i=0; i<4; i++) max_index_stage1[i] <= 0;
        end
    else
        begin
        for (integer i=0; i<4; i++) max_elem_stage1[i] <= max_elem_stage1_next[i];
        for (integer i=0; i<4; i++) max_index_stage1[i] <= max_index_stage1_next[i];
        end
    end
//// stage 2 ////
// intermediate signals declaration
logic [31:0] max_elem_stage2 [1:0];
logic [31:0] max_index_stage2 [1:0];
logic [31:0] max_elem_stage2_next [1:0];
logic [31:0] max_index_stage2_next [1:0];
// combinational logic
always @*
    begin
    for(integer i=0; i<2; i++)
        begin
        max_elem_stage2_next[i] = 0;
        max_index_stage2_next[i] = 0;
        if (max_elem_stage1[(i<<1)] > max_elem_stage1[(i<<1)+1])
            begin
            max_elem_stage2_next[i] = max_elem_stage1[(i<<1)];
            max_index_stage2_next[i] = max_index_stage1[(i<<1)];
            end
        else
            begin
            max_elem_stage2_next[i] = max_elem_stage1[(i<<1)+1];
            max_index_stage2_next[i] = max_index_stage1[(i<<1)+1];
            end
        end
    end
// writing to registers
always @(posedge clk_i)
    begin
    if (rst_i)
        begin
        for (integer i=0; i<2; i++) max_elem_stage2[i] <= 0;
        for (integer i=0; i<2; i++) max_index_stage2[i] <= 0;
        end
    else
        begin
        for (integer i=0; i<2; i++) max_elem_stage2[i] <= max_elem_stage2_next[i];
        for (integer i=0; i<2; i++) max_index_stage2[i] <= max_index_stage2_next[i];
        end
    end
//// stage 3 ////
// intermediate signals declaration
logic [31:0] max_elem_next;
logic [3:0] max_index_next;
// combinational logic
always @*
    begin
    max_elem_next = 0;
    max_index_next = 0;
    if (max_elem_stage2[0] > max_elem_stage2[1])
        begin
        max_elem_next = max_elem_stage2[0];
        max_index_next = max_index_stage2[0];
        end
    else
        begin
        max_elem_next = max_elem_stage2[1];
        max_index_next = max_index_stage2[1];
        end
    end
// writing to registers
always @(posedge clk_i)
    begin
    if (rst_i)
        begin
        max_elem_bo <= 0;
        max_index_bo <= 0;
        end
    else
        begin
        max_elem_bo <= max_elem_next;
        max_index_bo <= max_index_next;
        end
    end
endmodule
