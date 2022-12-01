#define IO_LED (*(volatile unsigned int *)(0x80000000))
#define IO_SW (*(volatile unsigned int *)(0x80000004))
unsigned int FindMaxVal(unsigned int* max_index, unsigned int datain[16])
{
    unsigned int max_val = 0;
    *max_index = 0;
    for (int i = 0; i < 16; i++) {
        if (datain[i] > max_val) {
            max_val = datain[i];
            *max_index = i;
        }
    }
    return max_val;
}
//--------------------------------------------------------------------------
// Main
int main(int argc, char* argv[])
{
    unsigned int max_index;
    unsigned int max_val;
    unsigned int datain[16] = { 0x112233cc, 0x55aa55aa, 0x01010202, 0x44556677,
0x00000003, 0x00000004, 0x00000005, 0x00000006, 0x00000007, 0xdeadbeef, 0xfefe8800,
0x23344556, 0x05050505, 0x07070707, 0x99999999, 0xbadc0ffe };
    IO_LED = 0x55aa55aa;
    max_val = FindMaxVal(&max_index, datain);
    IO_LED = max_index;
    IO_LED = max_val;
    while (1) {} // infinite loop
    return 0;
}