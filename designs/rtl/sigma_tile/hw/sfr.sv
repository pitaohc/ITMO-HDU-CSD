/*
 * sfr.sv
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module sfr
#(
	parameter corenum=0
	, parameter CPU_RESET_DEFAULT=0
	, parameter IRQ_NUM_POW = 4
)
(
	input [0:0] clk_i
	, input [0:0] rst_i

	, MemSplit32.Slave host

	, output logic cpu_reset_o

	, output logic irq_timer

	, output logic sgi_req_o
	, output logic [IRQ_NUM_POW-1:0] sgi_code_bo
);

localparam IDCODE_ADDR 			= 8'h00;
localparam CTRL_ADDR 			= 8'h04;
localparam CORENUM_ADDR 		= 8'h08;
localparam SGI_ADDR 			= 8'h0C;
localparam TIMER_CTRL_ADDR 		= 8'h10;
localparam TIMER_PERIOD_ADDR 	= 8'h14;

logic cpu_reset;
always @(posedge clk_i) cpu_reset_o <= rst_i | cpu_reset;

logic timer_inprogress, timer_reload;
logic [31:0] timer_period;
logic [31:0] timer_value;

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		host.resp <= 1'b0;
		cpu_reset <= CPU_RESET_DEFAULT;
		irq_timer <= 1'b0;
		sgi_req_o <= 0;
		sgi_code_bo <= 0;
		timer_inprogress <= 1'b0;
		timer_reload <= 1'b0;
		timer_period <= 0;
		timer_value <= 0;
		end
	else
		begin
		host.resp <= 1'b0;
		sgi_req_o <= 0;
		irq_timer <= 1'b0;

		if (timer_inprogress)
			begin
			if (timer_value == timer_period)
				begin
				timer_inprogress <= timer_reload;
				irq_timer <= 1'b1;
				timer_value <= 0;
				end
			else timer_value <= timer_value + 1;
			end

		if (host.req)
			begin
			if (host.we)
				begin
				if (host.addr[7:0] == CTRL_ADDR)
					begin
					cpu_reset <= host.wdata[0];
					end
				if (host.addr[7:0] == SGI_ADDR)
					begin
					sgi_req_o <= 1;
					sgi_code_bo <= host.wdata;
					end
				if (host.addr[7:0] == TIMER_CTRL_ADDR)
					begin
					timer_value <= 0;
					timer_inprogress <= host.wdata[0];
					timer_reload <= host.wdata[1];
					end
				if (host.addr[7:0] == TIMER_PERIOD_ADDR)
					begin
					timer_period <= host.wdata;
					end
				end
			else
				begin
				host.resp <= 1'b1;
				if (host.addr[7:0] == IDCODE_ADDR)  		host.rdata <= 32'hdeadbeef;
				if (host.addr[7:0] == CTRL_ADDR)    		host.rdata <= {31'h0, cpu_reset};
				if (host.addr[7:0] == CORENUM_ADDR) 		host.rdata <= corenum;
				if (host.addr[7:0] == TIMER_CTRL_ADDR) 		host.rdata <= {30'h0, timer_reload, timer_inprogress};
				if (host.addr[7:0] == TIMER_PERIOD_ADDR) 	host.rdata <= timer_period;
				end
			end
		end
	end

assign host.ack = host.req;

endmodule
