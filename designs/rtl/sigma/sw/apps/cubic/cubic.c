#define IO_LED (*(volatile unsigned int *)(0x80000000))
#define IO_SW (*(volatile unsigned int *)(0x80000004))

#define RELEASE
#ifdef DEBUG
#include<stdio.h>
#endif

unsigned int Cubic(unsigned int x)
{
    int y = 0, b = 0;
    for (int s = 30; s >= 0; s -= 3)
    {
        y *= 2;
        b = (3 * y * (y + 1) + 1) << s;
        if (x >= b)
        {
            x -= b;
            ++y;
        }
    }
    return y;
}

//--------------------------------------------------------------------------
// Main
int main(int argc, char* argv[])
{
    unsigned int x = 0, y = 0;
#ifdef DEBUG
    for (int i = 0; i <= 28; ++i)
    {
        x = i;
        y = Cubic(x);
        printf("%d cubic = %d\n", x, y);
    }
#else
    for (int i = 0; i <= 100; i += 10)
    {
        x = i;
        y = Cubic(x);
        IO_LED = y;
    }
    while (1) {}
#endif
    return 0;

    //     unsigned int max_index;
    //     unsigned int max_val;
    //     unsigned int datain[16] = { 0x112233cc, 0x55aa55aa, 0x01010202, 0x44556677,
    // 0x00000003, 0x00000004, 0x00000005, 0x00000006, 0x00000007, 0xdeadbeef, 0xfefe8800,
    // 0x23344556, 0x05050505, 0x07070707, 0x99999999, 0xbadc0ffe };
    //     IO_LED = 0x55aa55aa;
    //     max_val = FindMaxVal(&max_index, datain);
    //     IO_LED = max_index;
    //     IO_LED = max_val;
    //     while (1) {} // infinite loop
    //     return 0;
}