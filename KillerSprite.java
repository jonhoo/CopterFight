import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;

import javax.game.sidescroller.GamePanel;
import javax.game.sidescroller.Sprite;
import javax.media.utils.loaders.images.ImageAnimator;

public class KillerSprite extends Sprite {

    private final static int MOVEMENT_MAX = 6;
    private final static int FASTER_EVERY = 1000;
    private final static Rectangle hitbox = new Rectangle ( 0, 6, 32, 23 );
    private int ticks = 0;

    public KillerSprite ( ImageAnimator spriteImage, Point initialPosition, GamePanel game ) {
        super ( spriteImage, initialPosition, game );
        this.addHitbox ( KillerSprite.hitbox );
        this.setRandomSpeed ( );
    }

    @Override
    public Set<Sprite> leavingGameArea ( ) {
        Rectangle worldArea = this.game.getWorldArea ( );
        if ( this.position.x <= worldArea.x || this.position.x + this.getRectangle ( ).width >= worldArea.x + worldArea.width )
            this.setXSpeed ( -1 * this.getXSpeed ( ) );
        if ( this.position.y <= worldArea.y || this.position.y + this.getRectangle ( ).height >= worldArea.y + worldArea.height )
            this.setYSpeed ( -1 * this.getYSpeed ( ) );
        return null;
    }

    private int getRandomSpeed ( int min, int max ) {
        if ( Math.random ( ) < 0.5 )
            return (int) ( min + Math.random ( ) * max );
        else
            return -1 * (int) ( min + Math.random ( ) * max );
    }

    public void setRandomSpeed ( ) {
        this.setSpeed ( this.getRandomSpeed ( 1, MOVEMENT_MAX / 2 ), this.getRandomSpeed ( 1, MOVEMENT_MAX / 2 ) );
    }

    @Override
    public void tick ( ) {
        super.tick ( );

        if ( ++this.ticks % FASTER_EVERY == 0 && this.getSpriteImage ( ).getImageHolder ( ) != this.game.images.getHolder ( "explosion" ) ) {
            this.setXSpeed ( this.getRandomSpeed ( this.getXSpeed ( ), MOVEMENT_MAX ) );
            this.setYSpeed ( this.getRandomSpeed ( this.getYSpeed ( ), MOVEMENT_MAX ) );
        }
    }
}
