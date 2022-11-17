`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2022/11/16 20:57:05
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

logic [31:0] x[10:0];
logic [31:0] y[10:0];
logic [31:0] x_next[10:0];
logic [31:0] y_next[10:0];
logic [31:0] b[10:0];
always @*  begin
    x[0] = input_number;
    y[0] = 32'h0;
    b[0] = (3 * y[0] * (y[0] + 1) + 1) << 30;
    if(x[0]>=b[0])begin
        x[0] -= b[0];
        ++y[0];
    end
end

always @*  begin
    x[1] = x_next[0];
    y[1] = y_next[0];
    b[1] = (3 * y[1] * (y[1] + 1) + 1) << 27;
    if(x[1]>=b[1])begin
        x[1] -= b[1];
        ++y[1];
    end
end

always @*  begin
    x[2] = x_next[1];
    y[2] = y_next[1];
    b[2] = (3 * y[2] * (y[2] + 1) + 1) << 24;
    if(x[2]>=b[2])begin
        x[2] -= b[2];
        ++y[2];
    end
end

always @*  begin
    x[3] = x_next[2];
    y[3] = y_next[2];
    y[3] <<= 1;
    b[3] = (3 * y[3] * (y[3] + 1) + 1) << 21;
    if(x[3]>=b[3])begin
        x[3] -= b[3];
        ++y[3];
    end
end

always @*  begin
    x[4] = x_next[3];
    y[4] = y_next[3];
    y[4] <<= 1;
    b[4] = (3 * y[4] * (y[4] + 1) + 1) << 18;
    if(x[4]>=b[4])begin
        x[4] -= b[4];
        ++y[4];
    end
end

always @*  begin
    x[5] = x_next[4];
    y[5] = y_next[4];
    y[5] <<= 1;
    b[5] = (3 * y[5] * (y[5] + 1) + 1) << 15;
    if(x[5]>=b[5])begin
        x[5] -= b[5];
        ++y[5];
    end

    // y[5] <<= 1;
    // b[5] = (3 * y[5] * (y[5] + 1) + 1) << 12;
    // if(x[5]>=b[5])begin
    //     x[5] -= b[5];
    //     ++y[5];
    // end
end

// always @*  begin
//     x[6] = x_next[5];
//     y[6] = y_next[5];
//     y[6] <<= 1;
//     b[6] = (3 * y[6] * (y[6] + 1) + 1) << 12;
//     if(x[6]>=b[6])begin
//         x[6] -= b[6];
//         ++y[6];
//     end
// end

// always @*  begin
//     x[7] = x_next[6];
//     y[7] = y_next[6];
//     y[7] <<= 1;
//     b[7] = (3 * y[7] * (y[7] + 1) + 1) << 9;
//     if(x[7]>=b[7])begin
//         x[7] -= b[7];
//         ++y[7];
//     end
// end

// always @*  begin
//     x[8] = x_next[7];
//     y[8] = y_next[7];
    
//     for (integer s = 32'd6; s>=0; s-=3) begin
//         y[8] <<= 1;
//         b[8] = (3 * y[8] * (y[8] + 1) + 1) << s;
//         if(x[8]>=b[8])begin
//             x[8] -= b[8];
//             ++y[8];
//         end
//     end
// end
always @*  begin
    x[6] = x_next[5];
    y[6] = y_next[5];
    
    for (integer s = 32'd12; s>=0; s-=3) begin
        y[6] <<= 1;
        b[6] = (3 * y[6] * (y[6] + 1) + 1) << s;
        if(x[6]>=b[6])begin
            x[6] -= b[6];
            ++y[6];
        end
    end
end


always @(posedge clk_i ) begin
    if(rst_i) begin
        output_number <= 0;
        for (integer i=0; i<=10; i++) x_next[i] <= 0;
        for (integer i=0; i<=10; i++) y_next[i] <= 0;

    end
    else begin
        output_number <= y[6];
        for (integer i=0; i<=10; i++) x_next[i] <= x[i];
        for (integer i=0; i<=10; i++) y_next[i] <= y[i];
        
    end
end


endmodule
