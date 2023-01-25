
//********************************************************************
// Project Samplers: takes up texture slots [16 - 31]
/*
#define PROJECT_SAMPLERS_2D 16

uniform sampler2D[PROJECT_SAMPLERS_2D] u_project_textures;

sampler2D colorSampler()                { return u_project_textures[0];}
sampler2D detailSampler0()              { return u_project_textures[1];}
sampler2D volumeSampler0()              { return u_project_textures[2];}
sampler2D specularSampler0()            { return u_project_textures[3];}
sampler2D emissiveSampler0()            { return u_project_textures[4];}
sampler2D detailSampler1()              { return u_project_textures[5];}
sampler2D volumeSampler1()              { return u_project_textures[6];}
sampler2D specularSampler1()            { return u_project_textures[7];}
sampler2D emissiveSampler1()            { return u_project_textures[8];}
sampler2D depthSampler()                { return u_project_textures[9];}
sampler2D normalSampler()               { return u_project_textures[10];}
sampler2D shadowSampler()               { return u_project_textures[11];}
sampler2D previewSampler()              { return u_project_textures[12];}

vec4 fetchColor(ivec2 texel)            { return texelFetch(colorSampler(),texel,0);}
vec4 fetchDetail0(ivec2 texel)          { return texelFetch(detailSampler0(),texel,0);}
vec4 fetchvolume0(ivec2 texel)          { return texelFetch(volumeSampler0(),texel,0);}
vec4 fetchSpecular0(ivec2 texel)        { return texelFetch(specularSampler0(),texel,0);}
vec4 fetchEmissive0(ivec2 texel)        { return texelFetch(emissiveSampler0(),texel,0);}
vec4 fetchDetail1(ivec2 texel)          { return texelFetch(detailSampler1(),texel,0);}
vec4 fetchvolume1(ivec2 texel)          { return texelFetch(volumeSampler1(),texel,0);}
vec4 fetchSpecular1(ivec2 texel)        { return texelFetch(specularSampler1(),texel,0);}
vec4 fetchEmissive1(ivec2 texel)        { return texelFetch(emissiveSampler1(),texel,0);}
vec4 fetchDepth(ivec2 texel)            { return texelFetch(depthSampler(),texel,0);}
vec4 fetchNormal(ivec2 texel)           { return texelFetch(normalSampler(),texel,0);}
vec4 fetchShadow(ivec2 texel)           { return texelFetch(shadowSampler(),texel,0);}
vec4 fetchPreview(ivec2 texel)          { return texelFetch(previewSampler(),texel,0);}

vec4 sampleColor(vec2 uv)               { return texture(colorSampler(),uv);}
vec4 sampleDetail0(vec2 uv)             { return texture(detailSampler0(),uv);}
vec4 samplevolume0(vec2 uv)             { return texture(volumeSampler0(),uv);}
vec4 sampleSpecular0(vec2 uv)           { return texture(specularSampler0(),uv);}
vec4 sampleEmissive0(vec2 uv)           { return texture(emissiveSampler0(),uv);}
vec4 sampleDetail1(vec2 uv)             { return texture(detailSampler1(),uv);}
vec4 samplevolume1(vec2 uv)             { return texture(volumeSampler1(),uv);}
vec4 sampleSpecular1(vec2 uv)           { return texture(specularSampler1(),uv);}
vec4 sampleEmissive1(vec2 uv)           { return texture(emissiveSampler1(),uv);}
vec4 sampleDepth(vec2 uv)               { return texture(depthSampler(),uv);}
vec4 sampleNormal(vec2 uv)              { return texture(normalSampler(),uv);}
vec4 sampleShadow(vec2 uv)              { return texture(shadowSampler(),uv);}
vec4 samplePreview(vec2 uv)             { return texture(previewSampler(),uv);}

//********************************************************************
*/