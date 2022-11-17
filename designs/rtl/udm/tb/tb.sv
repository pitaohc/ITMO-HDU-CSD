/*
 * tb.v
 *
 *  Created on: 17.10.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`timescale 1ns / 1ps

`define HALF_PERIOD			5						//external 100 MHZ
`define DIVIDER_115200		32'd8680
`define DIVIDER_19200		32'd52083
`define DIVIDER_9600		32'd104166
`define DIVIDER_4800		32'd208333
`define DIVIDER_2400		32'd416666


module tb ();
//
logic CLK_100MHZ, RST, rx;
logic [15:0] SW;
logic [15:0] LED;

always #`HALF_PERIOD CLK_100MHZ = ~CLK_100MHZ;

always #1000 SW = SW + 8'h1;
	
NEXYS4_DDR
#(
	.SIM("YES")
) DUT (
	.CLK100MHZ(CLK_100MHZ)
    , .CPU_RESETN(!RST)
    
    , .SW(SW)
    , .LED(LED)

    , .UART_TXD_IN(rx)
    , .UART_RXD_OUT()
);

////reset all////
task RESET_ALL ();
    begin
    CLK_100MHZ = 1'b0;
    RST = 1'b1;
    rx = 1'b1;
    #(`HALF_PERIOD/2);
    RST = 1;
    #(`HALF_PERIOD*6);
    RST = 0;
    while (DUT.srst) WAIT(10);
    end
endtask

////wait////
task WAIT
    (
     input logic [15:0] periods
     );
    begin
    integer i;
    for (i=0; i<periods; i=i+1)
        begin
        #(`HALF_PERIOD*2);
        end
    end
endtask

`define UDM_RX_SIGNAL rx
`define UDM_BLOCK DUT.udm
`include "udm.svh"
udm_driver udm;

/////////////////////////
// main test procedure //
localparam CSR_LED_ADDR         = 32'h00000000;
localparam CSR_SW_ADDR          = 32'h00000004;
localparam TESTMEM_ADDR         = 32'h80000000;

initial
    begin
    logic [31:0] wrdata [];
    integer ARRSIZE=10;
    
	$display ("### SIMULATION STARTED ###");
	
	SW = 8'h30;
	RESET_ALL();
	//WAIT(100);
    //$display ("### 111111 ###");
	udm.cfg(`DIVIDER_115200, 2'b00);
	udm.check();
	udm.hreset();
	//WAIT(100);
	
    //$display ("### 222222 ###");
    // test data initialization
//    udm.wr32(32'h10000000, 32'd0000);
//    udm.rd32(32'h20000000);
    udm.wr32(32'h10000000, 32'd0010);
    udm.rd32(32'h20000000);
//    udm.wr32(32'h10000000, 32'd0020);
//    udm.rd32(32'h20000000);
//    udm.wr32(32'h10000000, 32'd0030);
//    udm.rd32(32'h20000000);
//    udm.wr32(32'h10000000, 32'd0040);
//    udm.rd32(32'h20000000);
//    udm.wr32(32'h10000000, 32'd0050);
//    udm.rd32(32'h20000000);
//    udm.wr32(32'h10000000, 32'd0060);
//    udm.rd32(32'h20000000);
//    udm.wr32(32'h10000000, 32'd0070);
//    udm.rd32(32'h20000000);
//    udm.wr32(32'h10000000, 32'd0080);
//    udm.rd32(32'h20000000);
//    udm.wr32(32'h10000000, 32'd0090);
//    udm.rd32(32'h20000000);
    udm.wr32(32'h10000000, 32'd0100);
    udm.rd32(32'h20000000);

	// writing to LED
	//udm.wr32(32'h00000000, 32'h5a5a5a5a);
	//WAIT(1000);
    //excepted answer  0xfefe8800 at index 10 (0xa).
	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
    end


endmodule
