package com.kaeruct.glxy.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ImageCache {

    public static TextureAtlas atlas;

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("data/textures.txt"),
                Gdx.files.internal("data"));
    }

    public static TextureRegion getTexture(String name) {
        return atlas.findRegion(name);
    }
}