import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import javax.game.sidescroller.GamePanel;
import javax.game.sidescroller.Sprite;
import javax.media.utils.loaders.images.ImageAnimator;

public class ProjectileSprite extends Sprite {
    
    private static final int SPEED = 10;

    public ProjectileSprite ( ImageAnimator spriteImage, Point initialPosition, GamePanel game, HeliSprite firedBy ) {
        super ( spriteImage, initialPosition, game );
        if ( firedBy.getXSpeed ( ) < 0 || firedBy.getSpriteImage ( ).getCurrentPosition ( ) == game.images.getGroupHolder ( "helicopter" ).getIndexOf ( "heli_still_left" ) )
            this.setXSpeed ( -1 * SPEED );
        else
            this.setXSpeed ( SPEED );
    }

    @Override
    public Set<Sprite> leavingGameArea ( ) {
        Set<Sprite> remove = new HashSet<Sprite> ( );
        remove.add ( this );
        return remove;
    }

}
