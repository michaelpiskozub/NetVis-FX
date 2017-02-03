package netvis.view.util.jogl.gameengine;

import javax.media.opengl.GL2;
import java.util.HashMap;

public class VertexBufferPool {
    protected static HashMap<String, VertexBuffer> vertexbuffers;

    // Singleton pattern version II - everything is static
    private VertexBufferPool() {}

    static {
        vertexbuffers = new HashMap<>();
    }

    public static void putBuffer(String name, VertexBuffer resource) {
        vertexbuffers.put (name, resource);
    }

    public static void discardAll() {
        for (VertexBuffer t : vertexbuffers.values()) {
            t.discard();
        }
    }

    public static VertexBuffer get(String buffername) {
        return vertexbuffers.get (buffername);
    }

    public static void rebind(GL2 gl) {
        // Rebind the textures
        for (VertexBuffer t : vertexbuffers.values()) {
            t.rebind(gl);
        }
    }
}