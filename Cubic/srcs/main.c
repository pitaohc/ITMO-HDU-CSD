// #define ARR_SIZE 16
// typedef struct
// {
// int max_elem;
// int max_index;
// } maxval_data;
// maxval_data FindMaxVal(unsigned int x[ARR_SIZE]);
unsigned int Cubic(unsigned int x);
int main()
{
    int x,y;
    for (int i = 0; i <= 100; i=i+10)
    {
        x = i;
        y = Cubic(x);
        printf("%d cubic = %d\n", x, y);
    }
    return 0;
}
