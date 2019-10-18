#include "io.h"
#include "isr.h"

#define ISR_SIZE 10
void (*isr[ISR_SIZE])() = {0};

void __int_handler (int mcause)
{
  if ((isr[mcause] != 0) && (mcause < ISR_SIZE)) {
    isr[mcause]();
  }
}

void ConnectISR( int cause_num, void (*new_isr)()) {
  isr[cause_num] = new_isr;
}