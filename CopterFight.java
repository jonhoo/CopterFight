import java.io.IOException;

import javax.game.sidescroller.GameBase;
import javax.media.utils.loaders.BadConfigurationLineException;

/**
 * See README.txt for details.
 * @author Jon Gjengset <jon@thesquareplanet.com>
 */
@SuppressWarnings ( "serial" )
public class CopterFight extends GameBase {
    public CopterFight ( ) throws IOException, BadConfigurationLineException {
        super ( "Copter Flight", new CopterFightPanel ( ) );
    }

    public static void main ( String[] args ) {
        try {
            new CopterFight ( );
        } catch ( IOException e ) {
            System.err.println ( "IO error: " + e.getMessage ( ) );
            System.exit ( 1 );
        } catch ( BadConfigurationLineException e ) {
            System.err.println ( "Configuration error: " + e.getMessage ( ) );
            System.exit ( 1 );
        }
    }
}