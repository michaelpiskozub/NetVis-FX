package netvis.view.util.jogl.gameengine;

import javax.media.opengl.GL2;
import java.util.ArrayList;
import java.util.List;

public class FramebufferPool {
    protected static List<Framebuffer> buffers;
    static {
        buffers = new ArrayList<>();
    }

    // Singleton pattern version II - everything is static
    private FramebufferPool() {}

    public static int generate() {
        Framebuffer fb = new Framebuffer(400);

        buffers.add(fb);
        return buffers.size() - 1;
    }

    public static Framebuffer get(int fbid) {
        if (fbid < buffers.size()) {
            return buffers.get(fbid);
        }

        return null;
    }

    public static void discardAll() {
        for (Framebuffer i : buffers) {
            i.discard();
        }
    }

    public static void regenerateAll(GL2 gl) {
        for (Framebuffer i : buffers) {
            i.create(gl);
        }
    }
}
