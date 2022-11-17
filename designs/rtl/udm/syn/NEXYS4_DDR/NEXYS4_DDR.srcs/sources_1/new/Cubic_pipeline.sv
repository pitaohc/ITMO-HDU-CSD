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

/* stage 0 */
always @*  begin
    integer b;
    integer stage = 0;
    
    x[stage] = input_number;
    y[stage] = 0;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 30;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end

end



/* stage 1 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd1;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 27;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 2 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd2;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 24;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 3 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd3;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 21;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 4 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd4;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 18;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 5 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd5;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 15;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 6 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd6;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 12;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 7 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd7;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 9;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 8 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd8;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 6;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 9 */
always @*  begin
    integer b=32'd0;
    integer stage = 32'd9;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];
    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 3;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end
end


/* stage 10 */
always @*  begin
    integer b;
    integer stage = 10;
    x[stage] = x_next[stage-1];
    y[stage] = y_next[stage-1];

    y[stage] <<= 1;
    b = (3 * y[stage] * (y[stage] + 1) + 1) << 0;
    if(x[stage]>=b)begin
        x[stage] -=  b;
        ++y[stage];
    end

end

always @(posedge clk_i ) begin
    if(rst_i) begin
        output_number <= 0;
        for (integer i=0; i<=10; i++) x_next[i] <= 0;
        for (integer i=0; i<=10; i++) y_next[i] <= 0;

    end
    else begin
        output_number <= y[10];
        for (integer i=0; i<=10; i++) x_next[i] <= x[i];
        for (integer i=0; i<=10; i++) y_next[i] <= y[i];
    end
end


endmodule
