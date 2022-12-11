`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2022/12/09 23:12:32
// Design Name: 
// Module Name: Cubic_pipeline
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


module Cubic_pipeline(
    input clk_i,
    input rst_i,
    input [31:0] input_number,
    output logic [31:0] output_number
);
logic [31:0] b[6];
logic [31:0] x[6];
logic [31:0] y[6];
// stage 0
always @(posedge clk_i)begin
    x[0] <= input_number;
    y[0] <= 0;
    for(integer s=30; s>=27; s-=3)begin
        y[0] <<= 1;
        b[0] = (3 * y[0] * (y[0] + 1) + 1) << s;
        if(x[0] >= b[0]) begin
            x[0] -= b[0];
            ++y[0];
        end
    end
end

// stage 1
always @(posedge clk_i)begin
    x[1] <= x[0];
    y[1] <= y[0];

    for(integer s=24; s>=21; s-=3) begin
        y[1] <<= 1;
        b[1] = (3 * y[1] * (y[1] + 1) + 1) << s;
        if(x[1] >= b[1]) begin
            x[1] -= b[1];
            ++y[1];
        end
    end
end

// stage 2
always @(posedge clk_i) begin
    x[2] <= x[1];
    y[2] <= y[1];

    for(integer s=18; s>=15; s-=3) begin
        y[2] <<= 1;
        b[2] = (3 * y[2] * (y[2] + 1) + 1) << s;
        if(x[2] >= b[2]) begin
            x[2] -= b[2];
            ++y[2];
        end
    end
end

// stage 3
always @(posedge clk_i) begin
    x[3] <= x[2];
    y[3] <= y[2];

    for(integer s=12; s>=9; s-=3) begin
        y[3] <<= 1;
        b[3] = (3 * y[3] * (y[3] + 1) + 1) << s;
        if(x[3] >= b[3]) begin
            x[3] -= b[3];
            ++y[3];
        end
    end
end

// stage 4
always @(posedge clk_i) begin
    x[4] <= x[3];
    y[4] <= y[3];

    for(integer s=6; s>=3; s-=3) begin
        y[4] <<= 1;
        b[4] = (3 * y[4] * (y[4] + 1) + 1) << s;
        if(x[4] >= b[4]) begin
            x[4] -= b[4];
            ++y[4];
        end
    end
end
// stage 5
always @(posedge clk_i) begin
    x[5] <= x[4];
    y[5] <= y[4];

    for(integer s=0; s>=0; s-=3) begin
        y[5] <<= 1;
        b[5] = (3 * y[5] * (y[5] + 1) + 1) << s;
        if(x[5] >= b[5]) begin
            x[5] -= b[5];
            ++y[5];
        end
    end
    output_number = y[5];
end
endmodule
