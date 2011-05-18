import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.game.SpriteListener;
import javax.game.sidescroller.GamePanel;
import javax.game.sidescroller.Ribbon;
import javax.game.sidescroller.RibbonsManager;
import javax.game.sidescroller.Sprite;
import javax.game.sidescroller.SpriteManager;
import javax.media.utils.loaders.BadConfigurationLineException;
import javax.media.utils.loaders.images.ImageAnimator;
import javax.media.utils.loaders.images.ImageLoader;
import javax.media.utils.loaders.images.ImageWatcher;
import javax.media.utils.loaders.sound.SoundLoader;

@SuppressWarnings ( "serial" )
public class CopterFightPanel extends GamePanel implements KeyListener, SpriteListener {
    private static final String IMAGE_CONFIG = "/images/config";
    private static final String SOUND_CONFIG = "/sounds/config";

    private static final int START_LIVES = 3;
    private static final int COIN_COUNT = 20;
    private static final int KILLER_COUNT = 16;
    private static final int LIFE_EVERY = 15;
    private static final Dimension PANEL_SIZE = new Dimension ( 800, 600 );
    private static final Rectangle WORLD = new Rectangle ( -1 * PANEL_SIZE.width, -1 * PANEL_SIZE.height, 2 * PANEL_SIZE.width, 2 * PANEL_SIZE.height );
    private static final int FIRE_INTERVAL = 10;

    private HeliSprite helicopter;

    private int lastPoints = 0;
    private int coinsHit = 0;
    private int killersHit = 0;
    private int lives = START_LIVES;
    private int lastFire = 0;

    private CoinSprite[] coins;
    private KillerSprite[] killers;

    private int activeFrames = 0;

    public CopterFightPanel ( ) throws IOException, BadConfigurationLineException {
        super (
                PANEL_SIZE,
                WORLD,
                new RibbonsManager ( ),
                new SpriteManager ( ),
                new ImageLoader ( CopterFightPanel.class.getResourceAsStream ( CopterFightPanel.IMAGE_CONFIG ) ),
                new SoundLoader ( CopterFightPanel.class.getResourceAsStream ( CopterFightPanel.SOUND_CONFIG ) ),
                50 );

        this.addKeyListener ( this );

        BufferedImage projectile = this.generateProjectileImage ( );
        this.images.loadImage ( "projectile", null, projectile, null );

        this.helicopter = new HeliSprite ( this.images.getGroupHolder ( "helicopter" ).getGroupAnimator ( 0 ), new Point ( 0, 0 ), this );
        this.sprites.addSprite ( this.helicopter );

        this.ticksPerUpdate = 1;
        this.ribbons.addRibbon ( new Ribbon ( this.images.getImage ( "background" ), 0, 0, new Point ( 0, 0 ) ) );
        this.ribbons.addRibbon ( new Ribbon ( this.images.getImage ( "clouds0" ), 1, 1, new Point ( 0, 0 ) ) );
        this.ribbons.addRibbon ( new Ribbon ( this.images.getImage ( "clouds1" ), 1.1, 1.1, new Point ( 0, 0 ) ) );
        this.ribbons.addRibbon ( new Ribbon ( this.images.getImage ( "clouds2" ), 1.2, 1.2, new Point ( 0, 0 ) ) );

        this.sprites.addCollisionWatcher ( this );

        /**
         * We can use one animator for all coins to make them all spin equally
         */
        this.coins = new CoinSprite[COIN_COUNT];
        ImageAnimator coin = this.images.getHolder ( "coin" ).getAnimator ( 100 );
        coin.setRepeating ( true );
        coin.start ( );
        for ( int i = 0; i < COIN_COUNT; i++ ) {
            this.coins[i] = new CoinSprite ( coin, new Point ( 0, 0 ), this );
            this.sprites.addSprite ( this.coins[i] );
        }

        this.killers = new KillerSprite[KILLER_COUNT];
        for ( int i = 0; i < KILLER_COUNT; i++ ) {
            ImageAnimator killer = this.images.getHolder ( "killer" ).getAnimator ( 100 );
            killer.setRepeating ( true );
            killer.start ( );
            this.killers[i] = new KillerSprite ( killer, new Point ( 0, 0 ), this );
            this.sprites.addSprite ( this.killers[i] );
        }

        this.sounds.getHolder ( "helicopter" ).setLooping ( true );

        this.resetGame ( );
        this.paused = true;
    }

    private BufferedImage generateProjectileImage ( ) {
        BufferedImage projectile = GraphicsEnvironment.getLocalGraphicsEnvironment ( ).getDefaultScreenDevice ( ).getDefaultConfiguration ( ).createCompatibleImage ( 8, 8, Transparency.BITMASK );
        Graphics2D g2d = projectile.createGraphics ( );
        Color transparent = new Color ( 0, 0, 0, 0 );
        g2d.setBackground ( transparent );
        g2d.setColor ( Color.black );
        g2d.setComposite ( AlphaComposite.Src );
        g2d.clearRect ( 0, 0, 8, 8 );
        g2d.fill ( new Ellipse2D.Float ( 1, 2, 6, 4 ) );
        g2d.dispose ( );
        return projectile;
    }

    public void resetGame ( ) {
        Point center = new Point ( this.worldArea.x + this.worldArea.width / 2, this.worldArea.y + this.worldArea.height / 2 );
        center.x -= this.images.getImage ( "helicopter" ).getWidth ( ) / 2;
        center.y -= this.images.getImage ( "helicopter" ).getHeight ( ) / 2;
        this.helicopter.moveTo ( center );

        for ( KillerSprite k : this.killers )
            k.moveTo ( CopterFightPanel.this.randomPosition ( ) );

        for ( CoinSprite c : this.coins )
            c.moveTo ( CopterFightPanel.this.randomPosition ( ) );

        this.lives = START_LIVES;
        this.activeFrames = 0;
        this.lastFire = 0;
        this.coinsHit = 0;
        this.lastPoints = 0;
        this.killersHit = 0;
    }

    private Point randomPosition ( ) {
        return new Point ( this.worldArea.x + (int) ( Math.random ( ) * this.worldArea.width ), this.worldArea.y + (int) ( Math.random ( ) * this.worldArea.height ) );
    }

    @Override
    public void keyPressed ( KeyEvent e ) {
        switch ( e.getKeyCode ( ) ) {
            case KeyEvent.VK_C:
                if ( !e.isControlDown ( ) )
                    break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_Q:
            case KeyEvent.VK_END:
                this.end ( );
                break;
        }
    }

    @Override
    public void keyReleased ( KeyEvent e ) {
    }

    @Override
    protected void render ( Graphics g ) {
        if ( this.isPaused ( ) ) {
            this.drawTitleScreen ( g );
        } else {
            Font verdana = new Font ( "Verdana", Font.PLAIN | Font.BOLD, 16 );
            g.setFont ( verdana );
            g.setColor ( Color.black );
            g.drawString ( "Score: " + ( ( this.coinsHit + this.killersHit ) * this.activeFrames ), 10, 26 );
            g.drawString ( "Lives: " + this.lives, 10, 46 );
            g.drawString ( "Coins: " + this.coinsHit, 10, 66 );
            g.drawString ( "Enemies killed: " + this.killersHit, 10, 86 );
        }
    }

    private void drawTitleScreen ( Graphics g ) {
        Dimension visible = this.getSize ( );

        g.setColor ( new Color ( 255, 255, 255, 192 ) );
        g.fillRect ( 0, 0, visible.width, visible.height );

        Point centerBase = new Point ( visible.width / 2, visible.height / 2 );

        System.out.println ( "Last points: " + this.lastPoints );
        if ( this.lastPoints > 0 ) {
            Font f = new Font ( "arial", Font.PLAIN | Font.BOLD, 32 );
            String s = String.format ( "Game over, final score: %d", this.lastPoints );
            g.setFont ( f );
            g.setColor ( new Color ( 192, 32, 32, 255 ) );
            g.drawString ( s, centerBase.x - g.getFontMetrics ( ).stringWidth ( s ) / 2, 120 );
        }

        BufferedImage title = this.images.getImage ( "copterfight" );
        g.drawImage ( title, centerBase.x - title.getWidth ( ) / 2, centerBase.y - title.getHeight ( ) / 2, null );
    }

    @Override
    public Point tick ( ) {
        if ( this.lives <= 0 ) {
            // Don't do this for first tick (when lives == -1)
            if ( this.lives == 0 ) {
                this.sounds.getHolder ( "gameover" ).play ( );
                this.lastPoints = ( this.coinsHit + this.killersHit ) * this.activeFrames;
            }
            this.pause ( );
        }
        if ( !this.isPaused ( ) )
            this.activeFrames++;
        Point heliCenter = new Point ( (int) Math.round ( this.helicopter.getRectangle ( ).getCenterX ( ) ), (int) Math.round ( this.helicopter.getRectangle ( ).getCenterY ( ) ) );
        return new Point ( heliCenter.x - this.getVisibleMapRectangle ( ).width / 2, heliCenter.y - this.getVisibleMapRectangle ( ).height / 2 );
    }

    @Override
    public void keyTyped ( KeyEvent e ) {
        if ( e.getKeyChar ( ) == 'r' ) {
            this.resetGame ( );
            if ( this.isPaused ( ) )
                this.resume ( );
        } else if ( e.getKeyChar ( ) == 'p' && this.lives > 0 ) {
            if ( this.isPaused ( ) )
                this.resume ( );
            else
                this.pause ( );
        } else if ( e.getKeyChar ( ) == KeyEvent.VK_SPACE && this.activeFrames - this.lastFire >= FIRE_INTERVAL ) {
            this.lastFire = this.activeFrames;
            ProjectileSprite p = new ProjectileSprite ( this.images.getHolder ( "projectile" ).getAnimator ( 0 ), this.helicopter.getPosition ( ), this, this.helicopter );
            this.sprites.addSprite ( p );
            this.sounds.getHolder ( "shot" ).play ( );
        }
    }

    @Override
    public Set<Sprite> handleCollision ( Sprite a, Sprite b ) {

        if ( a instanceof ProjectileSprite )
            return this.projectileHit ( (ProjectileSprite) a, b );
        else if ( b instanceof ProjectileSprite )
            return this.projectileHit ( (ProjectileSprite) b, a );

        if ( !( a instanceof HeliSprite ) && !( b instanceof HeliSprite ) )
            return null;

        if ( a instanceof CoinSprite )
            this.hitCoin ( (CoinSprite) a );
        else if ( b instanceof CoinSprite )
            this.hitCoin ( (CoinSprite) b );

        if ( a instanceof KillerSprite )
            this.hitBadGuy ( (KillerSprite) a );
        else if ( b instanceof KillerSprite )
            this.hitBadGuy ( (KillerSprite) b );

        return null;
    }

    private Set<Sprite> projectileHit ( ProjectileSprite projectile, Sprite target ) {
        if ( !( target instanceof KillerSprite ) )
            return null;

        this.explode ( (KillerSprite) target );
        Set<Sprite> remove = new HashSet<Sprite> ( );
        remove.add ( projectile );
        this.killersHit++;

        return remove;
    }

    @Override
    protected void onStart ( ) {
        this.sounds.getHolder ( "helicopter" ).play ( );
    }

    @Override
    protected void onPause ( ) {
        this.sounds.getHolder ( "helicopter" ).stop ( );
        this.drawTitleScreen ( this.getGraphics ( ) );
    }

    @Override
    protected void onResume ( ) {
        this.sounds.getHolder ( "helicopter" ).resume ( );
    }

    private void hitCoin ( CoinSprite a ) {
        a.moveTo ( this.randomPosition ( ) );
        this.sounds.getHolder ( "ding" ).play ( );
        this.coinsHit++;
        if ( this.coinsHit % LIFE_EVERY == 0 )
            this.lives++;
    }

    private void explode ( final KillerSprite a ) {
        if ( a.getSpriteImage ( ).getImageHolder ( ) == this.images.getHolder ( "explosion" ) )
            return;

        this.sounds.getHolder ( "crash" ).play ( );

        a.getSpriteImage ( ).destroy ( );
        a.setSpeed ( 0, 0 );
        a.changeImage ( this.images.getHolder ( "explosion" ).getAnimator ( 100 ) );
        a.getSpriteImage ( ).addWatcher ( new ImageWatcher ( ) {

            @Override
            public void sequenceEnded ( ImageAnimator holder ) {
                a.getSpriteImage ( ).destroy ( );
                a.changeImage ( CopterFightPanel.this.images.getHolder ( "killer" ).getAnimator ( 100 ) );
                a.moveTo ( CopterFightPanel.this.randomPosition ( ) );
                a.setRandomSpeed ( );
            }

            @Override
            public void sequenceLooped ( ImageAnimator holder ) {
            }

        } );
        a.getSpriteImage ( ).start ( );
    }

    private void hitBadGuy ( final KillerSprite a ) {
        if ( a.getSpriteImage ( ).getImageHolder ( ) == this.images.getHolder ( "explosion" ) )
            return;

        this.explode ( a );
        this.lives--;
    }
}
