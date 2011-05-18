import java.awt.Point;

import javax.game.sidescroller.GamePanel;
import javax.game.sidescroller.Sprite;
import javax.media.utils.loaders.images.ImageAnimator;


public class CoinSprite extends Sprite {

    public CoinSprite ( ImageAnimator spriteImage, Point initialPosition, GamePanel game ) {
        super ( spriteImage, initialPosition, game );
    }
}
