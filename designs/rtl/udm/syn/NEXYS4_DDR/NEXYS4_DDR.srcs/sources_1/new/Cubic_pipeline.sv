// `timescale 1ns / 1ps
// //////////////////////////////////////////////////////////////////////////////////
// // Company: 
// // Engineer: 
// // 
// // Create Date: 2020/04/23 16:09:05
// // Design Name: 
// // Module Name: Cubic_Root_pipelined
// // Project Name: 
// // Target Devices: 
// // Tool Versions: 
// // Description: 
// // 
// // Dependencies: 
// // 
// // Revision:
// // Revision 0.01 - File Created
// // Additional Comments:
// // 
// //////////////////////////////////////////////////////////////////////////////////


// module Cubic_pipeline(
//     input clk_i
//     , input rst_i
//     , input [31:0] elem_x
//     , output logic [31:0] elem_y
//     );
// // stage 0
// logic [31:0] b;
// logic [31:0] elem_x_copy_0;
// logic [31:0] elem_y_0;
// always @(posedge clk_i)
//     begin
//     elem_x_copy_0 <= elem_x;
//     elem_y_0 <= 0;
//     for(integer s=30;s>=27;s=s-3)
//         begin
//         elem_y_0 = 2*elem_y_0;
//         b = (3*elem_y_0*(elem_y_0 + 1) + 1) << s;
//         if(elem_x_copy_0 >= b)
//             begin
//             elem_x_copy_0 = elem_x_copy_0 - b;
//             elem_y_0 = elem_y_0 + 1;
//             end
//         end
//     end

// // stage 1
// logic [31:0] elem_x_copy_1;
// logic [31:0] elem_y_1;
// always @(posedge clk_i)
//     begin
//         elem_x_copy_1 <=elem_x_copy_0;
//         elem_y_1 <= elem_y_0;
//     for(integer s=24;s>=21;s=s-3)
//         begin
//         elem_y_1 = 2*elem_y_1;
//         b = (3*elem_y_1*(elem_y_1 + 1) + 1) << s;
//         if(elem_x_copy_1 >= b)
//             begin
//             elem_x_copy_1 = elem_x_copy_1 - b;
//             elem_y_1 = elem_y_1 + 1;
//             end
//         end
//     end

// // stage 2
// logic [31:0] elem_x_copy_2;
// logic [31:0] elem_y_2;
// always @(posedge clk_i)
//     begin
//         elem_x_copy_2 <=elem_x_copy_1;
//         elem_y_2 <= elem_y_1;
//     for(integer s=18;s>=15;s=s-3)
//         begin
//         elem_y_2 = 2*elem_y_2;
//         b = (3*elem_y_2*(elem_y_2 + 1) + 1) << s;
//         if(elem_x_copy_2 >= b)
//             begin
//             elem_x_copy_2 = elem_x_copy_2 - b;
//             elem_y_2 = elem_y_2 + 1;
//             end
//         end
//     end
// // stage 3
// logic [31:0] elem_x_copy_3;
// logic [31:0] elem_y_3;
// always @(posedge clk_i)
//     begin
//         elem_x_copy_3 <=elem_x_copy_2;
//         elem_y_3 <= elem_y_2;
//     for(integer s=12;s>=9;s=s-3)
//         begin
//         elem_y_3 = 2*elem_y_3;
//         b = (3*elem_y_3*(elem_y_3 + 1) + 1) << s;
//         if(elem_x_copy_3 >= b)
//             begin
//             elem_x_copy_3 = elem_x_copy_3 - b;
//             elem_y_3 = elem_y_3 + 1;
//             end
//         end
//     end

//    // stage 4
// logic [31:0] elem_x_copy_4;
// logic [31:0] elem_y_4;
// always @(posedge clk_i)
//     begin
//         elem_x_copy_4 <=elem_x_copy_3;
//         elem_y_4 <= elem_y_3;
//         for(integer s=6;s>=3;s=s-3) begin
//         elem_y_4 = 2*elem_y_4;
//         b = (3*elem_y_4*(elem_y_4 + 1) + 1) << s;
//         if(elem_x_copy_4 >= b)
//             begin
//             elem_x_copy_4 = elem_x_copy_4 - b;
//             elem_y_4 = elem_y_4 + 1;
//             end
//         end
//     elem_y <= elem_y_4;
//     end
 
//     // stage 5
// logic [31:0] elem_x_copy_5;
// logic [31:0] elem_y_5;
// always @(posedge clk_i)
//     begin
//         elem_x_copy_5 <=elem_x_copy_4;
//         elem_y_5 <= elem_y_4;
//         for(integer s=0;s>=0;s=s-3) begin
//         elem_y_5 = 2*elem_y_5;
//         b = (3*elem_y_5*(elem_y_5 + 1) + 1) << s;
//         if(elem_x_copy_5 >= b)
//             begin
//             elem_x_copy_5 = elem_x_copy_5 - b;
//             elem_y_5 = elem_y_5 + 1;
//             end
//         end
//     elem_y <= elem_y_5;
//     end

// endmodule


`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2022/11/18 19:41:08
// Design Name: 
// Module Name: Cubic_pipline
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
   integer stage,b,s;
   stage = 0;
   s = 30 - 3 * stage;
   x[stage] = input_number;
   y[stage] = 32'h0;
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 1;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 2;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 3;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 4;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 5;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 6;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 7;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 8;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 9;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

always @*  begin
   integer stage,b,s;
   stage = 10;
   s = 30 - 3 * stage;
   x[stage] = x_next[stage-1];
   y[stage] = y_next[stage-1];
   y[stage] <<= 1;
   b = (3 * y[stage] * (y[stage] + 1) + 1) << s;
   if(x[stage] >= b)begin
       x[stage] = x[stage] - b;
       y[stage] = y[stage] + 1;
   end
   else begin
       x[stage] = x[stage];
       y[stage] = y[stage];
   end
end

// writing to registers
always @(posedge clk_i)begin
   if (rst_i) begin
       for (integer i=0; i<11; i++) b[i] <= 0;
       for (integer i=0; i<11; i++) x[i] <= 0;
       for (integer i=0; i<11; i++) y[i] <= 0;
       for (integer i=0; i<11; i++) x_next[i] <= 0;
       for (integer i=0; i<11; i++) y_next[i] <= 0;
   end
   else begin
       for (integer i=0; i<11; i++) x_next[i] <= x[i];
       for (integer i=0; i<11; i++) y_next[i] <= y[i];
       output_number <= y[10];
   end
end
endmodule
