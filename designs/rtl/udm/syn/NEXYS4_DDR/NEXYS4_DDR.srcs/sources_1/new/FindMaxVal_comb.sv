//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2022/10/28 21:07:00
// Design Name: 
// Module Name: FindMaxVal_comb
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


module FindMaxVal_comb(
    input [31:0] elem_bi [15:0], //bi = bus input
    output logic [31:0] max_elem_bo, //bo = bus output
    output logic [3:0] max_index_bo
    );
always @*
    begin
    max_elem_bo = elem_bi[0];
    max_index_bo = 3'd0;
    for (integer i= 1; i<16;i++ ) begin
        if(elem_bi[i]>max_elem_bo) begin
            max_elem_bo = elem_bi[i];
            max_index_bo = i;
        end 
    end
    end
endmodule
