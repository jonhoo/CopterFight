import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.game.sidescroller.GamePanel;
import javax.game.sidescroller.Sprite;
import javax.media.utils.loaders.images.GroupImageAnimator;

public class HeliSprite extends Sprite {

    GroupImageAnimator helicopter;
    public static final int SPEED = 5;

    private final static Map<String, Set<Rectangle>> hitboxes = new HashMap<String, Set<Rectangle>> ( );
    static {
        Set<Rectangle> heli_right = new HashSet<Rectangle> ( );
        heli_right.add ( new Rectangle ( 3, 0, 30, 21 ) );
        heli_right.add ( new Rectangle ( 33, 5, 29, 16 ) );
        heli_right.add ( new Rectangle ( 62, 9, 24, 8 ) );
        heli_right.add ( new Rectangle ( 33, 21, 29, 21 ) );

        Set<Rectangle> heli_left = new HashSet<Rectangle> ( );
        heli_left.add ( new Rectangle ( 53, 0, 30, 21 ) );
        heli_left.add ( new Rectangle ( 24, 5, 29, 16 ) );
        heli_left.add ( new Rectangle ( 0, 9, 24, 8 ) );
        heli_left.add ( new Rectangle ( 24, 21, 29, 21 ) );

        Set<Rectangle> heli_still_right = new HashSet<Rectangle> ( );
        heli_still_right.add ( new Rectangle ( 69, 0, 19, 2 ) );
        heli_still_right.add ( new Rectangle (  7, 0, 62, 19 ) );
        heli_still_right.add ( new Rectangle ( 44, 19, 25, 13 ) );

        Set<Rectangle> heli_still_left = new HashSet<Rectangle> ( );
        heli_still_left.add ( new Rectangle (  7, 0, 19, 2 ) );
        heli_still_left.add ( new Rectangle ( 26, 0, 62, 19 ) );
        heli_still_left.add ( new Rectangle ( 26, 19, 25, 13 ) );

        HeliSprite.hitboxes.put ( "heli_right", heli_right );
        HeliSprite.hitboxes.put ( "heli_left", heli_left );
        HeliSprite.hitboxes.put ( "heli_still_right", heli_still_right );
        HeliSprite.hitboxes.put ( "heli_still_left", heli_still_left );
    }

    public HeliSprite ( GroupImageAnimator images, Point initialPoint, GamePanel game ) {
        super ( images, initialPoint, game );
        this.helicopter = images;
        this.helicopter.setCurrentImage ( "heli_still_right" );
    }

    @Override
    public void keyPressed ( KeyEvent e ) {
        switch ( e.getKeyCode ( ) ) {
            case KeyEvent.VK_LEFT:
                this.setXSpeed ( -1 * SPEED );
                break;
            case KeyEvent.VK_RIGHT:
                this.setXSpeed ( SPEED );
                break;
            case KeyEvent.VK_UP:
                this.setYSpeed ( -1 * SPEED );
                break;
            case KeyEvent.VK_DOWN:
                this.setYSpeed ( SPEED );
                break;
        }

        this.pickHeliImage ( );
    }

    private void pickHeliImage ( ) {
        String image = null;
        if ( this.getXSpeed ( ) > 0 )
            image = "heli_right";
        else if ( this.getXSpeed ( ) < 0 )
            image = "heli_left";

        String lastImage = this.helicopter.getCurrentImageName ( );
        if ( image == null )
            if ( lastImage.equals ( "heli_left" ) || lastImage.equals ( "heli_still_left" ) )
                image = "heli_still_left";
            else
                image = "heli_still_right";

        if ( !image.equals ( lastImage ) ) {
            if ( lastImage.equals ( "heli_still_left" )
                    || lastImage.equals ( "heli_still_right" )
                    || image.equals ( "heli_still_right" )
                    || image.equals ( "heli_still_left" ) ) {
                BufferedImage before = this.helicopter.getImageGroupHolder ( ).getImage ( lastImage );
                BufferedImage after = this.helicopter.getImageGroupHolder ( ).getImage ( image );
                this.moveBy ( ( before.getWidth ( ) - after.getWidth ( ) ) / 2, ( before.getHeight ( ) - after.getHeight ( ) ) / 2 );
            }
            this.helicopter.setCurrentImage ( image );

            this.clearHitboxes ( );
            this.addHitboxes ( HeliSprite.hitboxes.get ( image ) );
        }
    }

    @Override
    public void keyReleased ( KeyEvent e ) {
        switch ( e.getKeyCode ( ) ) {
            case KeyEvent.VK_LEFT:
                if ( this.getXSpeed ( ) < 0 )
                    this.setXSpeed ( 0 );
                break;
            case KeyEvent.VK_RIGHT:
                if ( this.getXSpeed ( ) > 0 )
                    this.setXSpeed ( 0 );
                break;
            case KeyEvent.VK_UP:
                if ( this.getYSpeed ( ) < 0 )
                    this.setYSpeed ( 0 );
                break;
            case KeyEvent.VK_DOWN:
                if ( this.getYSpeed ( ) > 0 )
                    this.setYSpeed ( 0 );
                break;
        }

        this.pickHeliImage ( );
    }
    
    @Override
    public Set<Sprite> leavingGameArea ( ) {
        super.leavingGameArea ( );
        this.setSpeed ( 0, 0 );
        return null;
    }
}
