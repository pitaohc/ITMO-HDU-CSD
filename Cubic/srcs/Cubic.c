// #define ARR_SIZE 16
// typedef struct
// {
//     int max_elem;
//     int max_index;
// } maxval_data;
// maxval_data FindMaxVal(unsigned int x[ARR_SIZE])
// {
//     #pragma HLS array_partition variable=x block factor=16
//     #pragma HLS INTERFACE ap_none port=x
//     #pragma HLS INTERFACE ap_vld port=ap_return register
//     maxval_data ret_data;
//     ret_data.max_elem = 0;
//     ret_data.max_index = 0;
//     for (int i=0; i<ARR_SIZE; i++) {
//         if (x[i] > ret_data.max_elem) {
//             ret_data.max_elem = x[i];
//             ret_data.max_index = i;
//         }
//     }
//     return ret_data;
// }

unsigned int Cubic(unsigned int x)
{
#pragma HLS INTERFACE ap_none port=x
#pragma HLS INTERFACE ap_vld port=ap_return register
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