`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: HuCan
// 
// Create Date: 2022/10/29 14:58:26
// Design Name: 
// Module Name: Cubic_comb
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


module Cubic_comb(
    input [31:0] input_number,
    output logic [31:0] output_number
    );
logic [31:0] x;
logic [31:0] b = 32'h0;
logic [31:0] y = 32'h0;
always @*  begin
    x = input_number;
    y = 32'h0;
    b = 32'h0;
    for (integer s = 32'd30; s>=0; s-=3) begin
        y = 2 * y;
        b = (3 * y * (y + 1) + 1) << s;
        if(x>=b)begin
            x -=  b;
            y++;
        end
    end
    output_number = y;
end
endmodule
