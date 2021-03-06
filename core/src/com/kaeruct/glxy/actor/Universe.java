package com.kaeruct.glxy.actor;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.kaeruct.glxy.data.ImageCache;
import com.kaeruct.glxy.model.Particle;
import com.kaeruct.glxy.model.Settings;
import com.kaeruct.glxy.model.Settings.Setting;

public class Universe extends Actor {
    final OrthographicCamera camera;
    final ShapeRenderer sr;
    final Particle protoParticle;
    final Array<Particle> particles;
    TrailParticleManager trailParticles;
    final Vector3 initPos, touchPos, cinitPos, ctouchPos;
    final CameraController controller;
    public final GestureDetector gestureDetector;

    private Particle followedParticle;

    public Settings settings;
    boolean addedParticle;
    public boolean panning;
    public boolean inMenu;

    public final float minRadius = 5;
    public final float maxRadius = 80;
    public final float minZoom = 0.1f;
    public final float maxZoom = 16;
    final float G = 0.1f; // gravity constant
    final float sG = G * 0.5f; // multiplier for slingshot
    final int maxTrails = 500; // max trails for all particles
    private Rectangle bottomBar = null;
    private Texture texture;
    private Texture bg;
    private Matrix4 m4;
    private long start = TimeUtils.millis();
    private int w;
    private int h;

    class CameraController implements GestureListener {
        float initialScale = 1;
        float px, py;

        public boolean touchDown(float x, float y, int pointer, int button) {
            initialScale = camera.zoom;
            return false;
        }

        private Particle getTouchedParticle(float x, float y) {
            Circle tapCircle = new Circle();
            for (Particle p : particles) {
                // check a slightly bigger area to allow for finger inaccuracy
                tapCircle.set(p.x, p.y, p.radius * 1.7f);
                if (tapCircle.contains(x, y)) {
                    return p;
                }
            }
            return null;
        }

        private void singleTap(float tapX, float tapY) {
            if (touchBottomBar(tapX, tapY))
                return;

            touchPos.set(tapX, tapY, 0);
            camera.unproject(touchPos);

            protoParticle.dragged = false;
            protoParticle.vel(new Vector3(0, 0, 0));
            protoParticle.position(touchPos);
            addParticle();
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            touchPos.set(x, y, 0);
            camera.unproject(touchPos);

            Particle p = getTouchedParticle(touchPos.x, touchPos.y);

            if (panning) {
                if (p == followedParticle) {
                    // stop following the followed particle if it was tapped
                    followedParticle = null;
                } else if (p != null) {
                    // follow a new particle
                    followedParticle = p;
                }
                return true;
            }

            if (followedParticle == null || p == null) {
                // single tap that wasn't either on another particle or the
                // one already being followed
                singleTap(x, y);
            }

            return true;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (panning) {
                camera.position.add(-deltaX * camera.zoom, -deltaY
                        * camera.zoom, 0);
            }

            return false;
        }

        @Override
        public boolean panStop(float v, float v1, int i, int i1) {
            return false;
        }

        @Override
        public boolean zoom(float originalDistance, float currentDistance) {
            float ratio = originalDistance / currentDistance;
            float z = initialScale * ratio;

            if (z <= maxZoom && z >= minZoom) {
                float zx = (px - getWidth() / 2) * (camera.zoom - z), zy = (py - getHeight() / 2)
                        * (camera.zoom - z);

                camera.translate(zx, zy);
                px = zx;
                py = zy;
                camera.zoom = z;
            }
            return false;
        }

        @Override
        public boolean pinch(Vector2 initialFirstPointer,
                Vector2 initialSecondPointer, Vector2 firstPointer,
                Vector2 secondPointer) {

            px = (initialFirstPointer.x + initialSecondPointer.x) / 2;
            py = (initialFirstPointer.y + initialSecondPointer.y) / 2;
            return false;
        }

        @Override
        public void pinchStop() {

        }

        public void update() {
            if (followedParticle != null) {
                camera.position.set(followedParticle.x, followedParticle.y, 0);
            }
        }
    }

    public Universe() {
        addedParticle = true;
        panning = false;
        particles = new Array<>();
        sr = new ShapeRenderer();
        touchPos = new Vector3();
        initPos = new Vector3();
        ctouchPos = new Vector3();
        cinitPos = new Vector3();

        protoParticle = (new Particle()).radius(minRadius);
        camera = new OrthographicCamera();

        controller = new CameraController();
        gestureDetector = new GestureDetector(20, 0.5f, 0.5f, 0.15f, controller);
        bottomBar = new Rectangle();

        settings = new Settings();
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();

        resize();
    }

    public void setBottomBar(float w, float h) {
        bottomBar.set(0, 0, w, h);
    }

    public void resize() {
        camera.setToOrtho(true, w, h);
        camera.position.set(w / 2f, h / 2f, 0);

        ImageCache.load();
        texture = ImageCache.getTexture("circle").getTexture();
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        trailParticles = new TrailParticleManager(maxTrails, texture);

        bg = new Texture(Gdx.files.internal("data/bg.png"));
        bg.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
    }

    public void setParticleRadius(float r) {
        protoParticle.radius = r;
    }

    @Override
    public void act(float delta) {
        manageInput();

        if (!settings.get(Setting.PAUSED)) {
            updateParticles();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        controller.update();
        camera.update();

        if (m4 == null) {
            m4 = batch.getProjectionMatrix().cpy();
        }

        // draw background
        batch.draw(bg, 0, 0, (int)Math.floor(camera.position.x/camera.zoom), (int)Math.floor(camera.position.y/camera.zoom), w, h);

        // draw particles and trails
        batch.setProjectionMatrix(camera.combined);

        renderParticles(batch);
        batch.end();

        // draw black bar on the bottom
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.6f);
        sr.rect(0, 0, bottomBar.width, bottomBar.height);
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (!panning && protoParticle.dragged) {
            // draw "slingshot" line

             sr.begin(ShapeType.Line);
             sr.setColor(Color.LIGHT_GRAY);
             float yoff = getTop();

             sr.line(cinitPos.x,
             yoff - cinitPos.y,
             ctouchPos.x,
             yoff - ctouchPos.y);
             sr.end();
        }
        batch.setProjectionMatrix(m4);
        batch.begin();
    }

    public void manageInput() {
        if (panning || inMenu)
            return;

        if (Gdx.input.isTouched(0) && !Gdx.input.isTouched(1)
                && !Gdx.input.justTouched() && // only one finger is touching
                !touchBottomBar(Gdx.input.getX(0), Gdx.input.getY(0))) {

            touchPos.set(Gdx.input.getX(0), Gdx.input.getY(0), 0);
            ctouchPos.set(touchPos);

            camera.unproject(touchPos);
            addedParticle = false;

            if (!protoParticle.dragged) {
                protoParticle.stop();
                protoParticle.dragged = true;
                initPos.set(touchPos);
                cinitPos.set(ctouchPos);
            }

            protoParticle.position(touchPos);
        } else if (!addedParticle) {
            if (protoParticle.dragged) {
                protoParticle.dragged = false;
                Vector3 vel = initPos.sub(touchPos).scl(sG);
                protoParticle.vel(vel);
                protoParticle.position(touchPos);
            }
            addParticle();
        }
    }

    private boolean touchBottomBar(float x, float y) {
        return bottomBar.contains(x, Gdx.graphics.getHeight() - y);
    }

    private void updateParticles() {
        Iterator<Particle> it;
        for (int i = 0; i < particles.size; i++) {
            Particle p = particles.get(i);
            if (p.dead)
                continue;

            for (int j = 0; j < particles.size; j++) {
                Particle p2 = particles.get(j);
                if (p2.dead || i == j)
                    continue;

                float dx = p2.x - p.x;
                float dy = p2.y - p.y;
                float d = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                if (d == 0)
                    d = (float) 0.001;

                if (p.collidesWith(p2)) {
                    if (settings.get(Setting.COLLISION)) {
                        // collision
                        float mtd = 2 * (p.radius + p2.radius - d) / d;

                        p2.inc(dx * mtd / p2.radius, dy * mtd / p2.radius);
                        p.inc(-dx * mtd / p.radius, -dy * mtd / p.radius);

                        p2.dx += dx * mtd / p2.mass;
                        p2.dy += dy * mtd / p2.mass;
                        p.dx -= dx * mtd / p.mass;
                        p.dy -= dy * mtd / p.mass;

                    } else {
                        Particle bigger = p.radius >= p2.radius ? p : p2, smaller = p.radius < p2.radius ? p
                                : p2;

                        bigger.radius((float) (p2.radius + Math
                                .sqrt(p.radius / 2)));
                        smaller.kill();

                        if (smaller == followedParticle) {
                            followedParticle = bigger;
                        }

                        if (p2 == smaller)
                            break;
                    }
                } else {
                    // "gravity"
                    float force = (float) (G * p.mass * p2.mass / Math
                            .pow(d, 2));
                    float fscale = force / d;
                    p.dx += fscale * dx / p.mass;
                    p.dy += fscale * dy / p.mass;
                }
            }
        }

        it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            if (!p.update()) {
                it.remove();
                fire(new ChangeEvent());
            }
        }
    }

    private void renderParticles(Batch batch) {
        if (followedParticle != null && !followedParticle.dead) {
            float r = 10f;
            // draw red circle around followed particle
            batch.setColor(0.9f, 0.2f, 0.2f, 1);
            batch.draw(
                texture,
                followedParticle.x - followedParticle.radius - r/2,
                followedParticle.y - followedParticle.radius - r/2,
                followedParticle.radius * 2 + r,
                followedParticle.radius * 2 + r
            );
        }

        for (Particle p : particles) {
            Color c = p.color;
            if (settings.get(Setting.TRAILS) && !settings.get(Setting.PAUSED)) {
                if (Math.abs(p.x - p.oldx) > 0.2
                        || Math.abs(p.y - p.oldy) > 0.2) {
                    trailParticles.add(p.x, p.y, p.radius, c);
                }
            }
            batch.setColor(c);
            batch.draw(texture, p.x - p.radius, p.y - p.radius, p.radius * 2,
                    p.radius * 2);
        }

        if (settings.get(Setting.TRAILS)) {
            trailParticles.render(batch, settings.get(Setting.PAUSED));
        }
    }

    private void addParticle() {
        long diff = TimeUtils.timeSinceMillis(start);
        boolean skipParticleAdd = diff < 250;

        if (skipParticleAdd) return; // avoid adding too fast
        if (Gdx.input.isTouched(0)) return; // do not add if finger is still down

        Particle p = new Particle(protoParticle);
        particles.add(p);
        addedParticle = true;
        start = TimeUtils.millis();

        protoParticle.vel(new Vector3(0, 0, 0)); // reset protoParticle vel

        fire(new ChangeEvent());
    }

    public void clearParticles() {
        particles.clear();
        clearTrails();
        followedParticle = null;

        fire(new ChangeEvent());
    }

    public void clearTrails() {
        trailParticles.clear();
    }

    public int getParticleCount() {
        return particles.size;
    }

    public void dispose() {
        sr.dispose();
    }

    public void resetZoom() {
        camera.zoom = 1;
    }
}
