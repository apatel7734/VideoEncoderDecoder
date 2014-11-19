package com.avgtechie.videoencoderdecoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by ashish on 11/13/14.
 */
public class SquareWithMemeTexture {

    private final String vs_SolidColor =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fs_SolidColor =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    /* SHADER Image
 *
 * This shader is for rendering 2D images straight from a texture
 * No additional effects.
 *
 */
    public static final String vs_Image =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    public static final String fs_Image =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                    "}";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer textureBuffer;

    private int mSolidColorProg;
    private int mTextureProg;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -0.5f, 0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f, 0.5f, 0.0f}; // top right

    private final short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public SquareWithMemeTexture(Context context) {
        mContext = context;
        initSolidColors();
        initTexture(context);
        initTexture();
    }

    private void initSolidColors() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = SurfaceRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vs_SolidColor);
        int fragmentShader = SurfaceRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fs_SolidColor);

        mSolidColorProg = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mSolidColorProg, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mSolidColorProg, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mSolidColorProg);                  // create OpenGL program executables
    }

    private void initTexture(Context context) {
        ByteBuffer tbb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        textureBuffer = tbb.asFloatBuffer();
        textureBuffer.put(squareCoords);
        textureBuffer.position(0);

        //prepares shaders for texture
        // prepare shaders and OpenGL program
        int vertexShader = SurfaceRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vs_Image);
        int fragmentShader = SurfaceRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fs_Image);

        mTextureProg = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mTextureProg, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mTextureProg, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mTextureProg);                  // create OpenGL program executables

        int id = context.getResources().getIdentifier("drawable/ic_launcher", null, context.getPackageName());
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);

        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        // We are done using the bitmap so we should recycle it.
        bmp.recycle();
    }


    public void drawImage(float[] mvpMatrix) {

        GLES20.glUseProgram(mSolidColorProg);

        // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mTextureProg, "vPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(mTextureProg, "a_texCoord");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, textureBuffer);

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(mTextureProg, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mvpMatrix, 0);

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation(mTextureProg, "s_texture");

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, squareCoords.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     *                  this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mSolidColorProg);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mSolidColorProg, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mSolidColorProg, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mSolidColorProg, "uMVPMatrix");
//        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
//        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    //drawing an image code starts below
    /**
     *
     */
    String vShaderStr =
            "attribute vec4 a_position;   \n"
                    + "attribute vec2 a_texCoord;   \n"
                    + "varying vec2 v_texCoord;     \n"
                    + "void main()                  \n"
                    + "{                            \n"
                    + "   gl_Position = a_position; \n"
                    + "   v_texCoord = a_texCoord;  \n"
                    + "}                            \n";

    String fShaderStr =
            "precision mediump float;                            \n"
                    + "varying vec2 v_texCoord;                            \n"
                    + "uniform sampler2D s_texture;                        \n"
                    + "void main()                                         \n"
                    + "{                                                   \n"
                    + "  gl_FragColor = texture2D( s_texture, v_texCoord );\n"
                    + "}                                                   \n";


    private int mProgramObject;

    // Attribute locations
    private int mPositionLoc;
    private int mTexCoordLoc;

    // Sampler location
    private int mSamplerLoc;

    // Texture handle
    private int mTextureId;

    // Additional member variables
    private FloatBuffer mVertices;
    private ShortBuffer mIndices;

    private final float[] mVerticesData =
            {
                    -0.5f, 0.5f, 0.0f, // Position 0
                    0.0f, 0.0f, // TexCoord 0
                    -0.5f, -0.5f, 0.0f, // Position 1
                    0.0f, 1.0f, // TexCoord 1
                    0.5f, -0.5f, 0.0f, // Position 2
                    1.0f, 1.0f, // TexCoord 2
                    0.5f, 0.5f, 0.0f, // Position 3
                    1.0f, 0.0f // TexCoord 3
            };

    private final short[] mIndicesData = {
            0, 1, 2, 0, 2, 3
    };

    private Context mContext;


    private void initTexture() {
        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mVerticesData).position(0);
        mIndices = ByteBuffer.allocateDirect(mIndicesData.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(mIndicesData).position(0);

        // Load the shaders and get a linked program object
        mProgramObject = ESShader.loadProgram(vShaderStr, fShaderStr);

        // Get the attribute locations
        mPositionLoc = GLES20.glGetAttribLocation(mProgramObject, "a_position");
        mTexCoordLoc = GLES20.glGetAttribLocation(mProgramObject, "a_texCoord");

        // Get the sampler location
        mSamplerLoc = GLES20.glGetUniformLocation(mProgramObject, "s_texture");

        // Load the texture
        mTextureId = createSimpleTexture2D();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }


    private int createSimpleTexture2D() {
        // Texture object handle
        int[] textureId = new int[1];


        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        // Use tightly packed data
        //GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);

        //  Generate a texture object
        GLES20.glGenTextures(1, textureId, 0);

        // Bind the texture object
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        //  Load the texture
        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, 2, 2, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);


        // Set the filtering mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        return textureId[0];
    }


    public void drawImage() {

        // Clear the color buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Use the program object
        GLES20.glUseProgram(mProgramObject);

        // Load the vertex position
        mVertices.position(0);
        GLES20.glVertexAttribPointer(mPositionLoc, 3, GLES20.GL_FLOAT,
                false,
                5 * 4, mVertices);
        // Load the texture coordinate
        mVertices.position(3);
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                5 * 4,
                mVertices);

        GLES20.glEnableVertexAttribArray(mPositionLoc);
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        // Bind the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        // Set the sampler texture unit to 0
        GLES20.glUniform1i(mSamplerLoc, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndices);
    }

}
