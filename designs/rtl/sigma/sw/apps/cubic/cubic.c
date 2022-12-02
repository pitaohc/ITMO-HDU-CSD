#define IO_LED (*(volatile unsigned int *)(0x80000000))
#define IO_SW (*(volatile unsigned int *)(0x80000004))

#define DEBUG
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
    unsigned int x = 10, y = 0;
#ifdef DEBUG
    for (int i = 0; i <= 28; ++i)
    {
        x = i;
        y = Cubic(x);
        printf("%d cubic = %d\n", x, y);
    }
#else
    IO_LED = 0x55aa55aa;
    IO_LED = Cubic(x);
    while (1) {}
#endif
    return 0;
}