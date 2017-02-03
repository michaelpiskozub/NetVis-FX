package netvis.view.util.jogl.gameengine;

import javax.media.opengl.GL2;
import java.net.URL;
import java.util.HashMap;

public class TexturePool {
    protected static HashMap<String, Texture> textures;

    // Singleton pattern version II - everything is static
    private TexturePool() {}

    static {
        textures = new HashMap<>();
    }

    public static void loadTexture(String name, URL resource) {
        textures.put(name, new Texture(resource));
    }

    public static void discardTextures() {
        for (Texture t : textures.values()) {
            t.discard();
        }
    }

    public static Texture get(String textureName) {
        return textures.get(textureName);
    }

    public static void rebind(GL2 gl) {
        // Rebind the textures
        for (Texture t : textures.values()) {
            t.rebind(gl);
        }
    }
}
