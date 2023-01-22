plugins {
    java
}

group = "io.github.heathensoft"
version = "1.0-SNAPSHOT"

repositories {
    //mavenCentral()
}

dependencies {
    implementation(files("libs/heathensoft-jlib-0.5.0.jar"))
    runtimeOnly(files("libs/natives/lwjgl-natives-windows.jar"))
    runtimeOnly(files("libs/natives/lwjgl-glfw-natives-windows.jar"))
    runtimeOnly(files("libs/natives/lwjgl-opengl-natives-windows.jar"))
    runtimeOnly(files("libs/natives/lwjgl-stb-natives-windows.jar"))
}

