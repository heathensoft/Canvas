
#define BRUSH_NUM_FUNCTIONS 7

#define BRUSH_FUNCTION_NON 0
#define BRUSH_FUNCTION_SET 1
#define BRUSH_FUNCTION_ADD 2
#define BRUSH_FUNCTION_SUB 3
#define BRUSH_FUNCTION_MIX 4
#define BRUSH_FUNCTION_SMO 5
#define BRUSH_FUNCTION_SHA 6


//********************************************************************
// Brush

#define BRUSH_BINDING_POINT 2

struct Brush {
    vec4 contour_color;
    int texture_size;
    int function;
    int color_value;
    int std140_padding;
};

layout (std140, binding = BRUSH_BINDING_POINT) uniform BrushBlock {
    Brush brush;
};

//********************************************************************

const float[9] SMOOTHEN_KERNEL = {
0.0625, 0.1250, 0.0625,
0.1250 ,0.2500 ,0.1250,
0.0625, 0.1250, 0.0625
};

const float[9] SHARPEN_KERNEL = {
-0.250, -1.000, -0.250,
-1.000,  5.0000,-1.000,
-0.250, -1.000, -0.250
};

float calculateColor(Brush brush, ivec2 texel, sampler2D back_buffer) {

    int function = brush.function >= BRUSH_NUM_FUNCTIONS ||
    brush.function < 0 ? BRUSH_FUNCTION_NON : brush.function;
    float brush_color = float((brush.color_value & 255) / 255.0);
    float back_buffer_color = texelFetch(back_buffer, texel, 0).r;
    float final_color = 0.0;

    if(function == BRUSH_FUNCTION_NON) {

        final_color = back_buffer_color;

    } else if(function == BRUSH_FUNCTION_SET) {

        final_color = brush_color;

    } else if(function == BRUSH_FUNCTION_ADD) {

        final_color = back_buffer_color + brush_color;

    } else if(function == BRUSH_FUNCTION_SUB) {

        final_color = back_buffer_color - brush_color;

    } else if(function == BRUSH_FUNCTION_MIX) {

        final_color = mix(brush_color,back_buffer_color,0.5);

    } else{
        float[9] kernel;
        ivec2 adjacent[9] = {
        ivec2(-1, 1), ivec2(0, 1), ivec2(1, 1),
        ivec2(-1, 0), ivec2(0, 0), ivec2(1, 0),
        ivec2(-1,-1), ivec2(0,-1), ivec2(1,-1) };

        if(function == BRUSH_FUNCTION_SMO) {

            kernel = SMOOTHEN_KERNEL;

        } else if(function == BRUSH_FUNCTION_SHA) {

            kernel = SHARPEN_KERNEL;
        }

        for(int i = 0; i < 9; i++) {
            final_color += texelFetch(back_buffer, texel + adjacent[i], 0).r * kernel[i];
        }
    }
    return clamp(final_color,0.0,1.0);
}

