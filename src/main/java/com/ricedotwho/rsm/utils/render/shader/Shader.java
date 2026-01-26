package com.ricedotwho.rsm.utils.render.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ricedotwho.rsm.utils.Accessor;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


public class Shader implements Accessor {

    private final int programID;

    public Shader(String fragmentShaderLoc) {
        this(fragmentShaderLoc, "/assets/shaders/vertex.vert");
    }

    public Shader(String fragmentShaderLoc, String vertexShaderLoc) {
        int program = glCreateProgram();

        int fragmentShaderID;

        fragmentShaderID = createShader(Shader.class.getResourceAsStream("/assets/shaders/" + fragmentShaderLoc), GL_FRAGMENT_SHADER);

        glAttachShader(program, fragmentShaderID);

        int vertexID = createShader(Shader.class.getResourceAsStream(vertexShaderLoc), GL_VERTEX_SHADER);
        glAttachShader(program, vertexID);



        GL20.glLinkProgram(program);
        int status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);

        if (status == 0) {
            throw new IllegalStateException("Shader failed to link!");
        }
        this.programID = program;
    }

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void draw() {
        Window window = mc.getWindow();
        float width = (float) window.getGuiScaledWidth();
        float height = (float) window.getGuiScaledHeight();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }


    public static void draw(float x, float y, float width, float height) {
        //gfx.fill((int) x, (int) y, (int) width, (int) height, color);
//        GL11.glBegin(GL_QUADS);
//        GL11.glTexCoord2f(0, 0);
//        GL11.glVertex2f(x, y);
//        GL11.glTexCoord2f(0, 1);
//        GL11.glVertex2f(x, y + height);
//        GL11.glTexCoord2f(1, 1);
//        GL11.glVertex2f(x + width, y + height);
//        GL11.glTexCoord2f(1, 0);
//        GL11.glVertex2f(x + width, y);
//        GL11.glEnd();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.addVertex(x, y, 0);
        buffer.addVertex(x, y, 0);
        buffer.addVertex(x + width, y, 0);
        buffer.addVertex(x + width, y + height, 0);
        buffer.addVertex(x, y + height, 0);
    }

    public static void bindTexture(int texture) {
        GlStateManager._bindTexture(texture);
    }

    public void load() {
        glUseProgram(programID);
    }

    public void unload() {
        glUseProgram(0);
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, readInputStream(inputStream));
        GL20.glCompileShader(shader);


        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            System.out.println(GL20.glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }

        return shader;
    }

    public void setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(programID, name);
        if (args.length > 1) glUniform2i(loc, args[0], args[1]);
        else glUniform1i(loc, args[0]);
    }

    public void setUniformfb(String name, FloatBuffer buffer) {
        GL20.glUniform1fv(glGetUniformLocation(programID, name), buffer);
    }

    public void setUniformf(String name, float... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1:
                GL20.glUniform1f(loc, args[0]);
                break;
            case 2:
                GL20.glUniform2f(loc, args[0], args[1]);
                break;
            case 3:
                GL20.glUniform3f(loc, args[0], args[1], args[2]);
                break;
            case 4:
                GL20.glUniform4f(loc, args[0], args[1], args[2], args[3]);
                break;
        }
    }

    public int getUniformf(String name) {
        return glGetUniformLocation(programID, name);
    }

}
