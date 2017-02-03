package netvis.view.util.jogl.gameengine;

import com.jogamp.common.nio.Buffers;

import javax.media.opengl.GL2;
import java.nio.FloatBuffer;

public class VertexBuffer {
    private int id;
    private FloatBuffer vertices;
    private FloatBuffer color;

    public VertexBuffer (float [] vert, int vnum) {
        id = -1;

        vertices = FloatBuffer.wrap(vert, 0, vnum*3);
    }

    public VertexBuffer (FloatBuffer vert) {
        id = -1;

        vertices = vert;
    }

    public int getId() {
        return id;
    }

    public int bind(GL2 gl) {
        if (id == -1) {
            create(gl);
        }

        return id;
    }

    public int rebind(GL2 gl) {
        id = -1;
        return bind(gl);
    }

    public void discard() {
        id = -1;
    }

    public void create(GL2 gl) {
        int [] arr = new int [1];

        gl.glGenBuffers(1, arr, 0);
        id = arr[0];

        // Bind to the new buffer
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, id);

        // Upload data
        gl.glBufferData (GL2.GL_ARRAY_BUFFER, vertices.capacity()* Buffers.SIZEOF_FLOAT, vertices, GL2.GL_STATIC_DRAW);
        //gl.glMapBuffer(GL2.GL_ARRAY_BUFFER, GL2.GL_READ_ONLY);
    }
}
