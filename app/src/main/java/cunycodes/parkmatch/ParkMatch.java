/* Configure Stormpath and enable logging for debug builds.
   Documentation: https://github.com/stormpath/stormpath-sdk-android
 */
package cunycodes.parkmatch;
        import com.stormpath.sdk.Stormpath;
        import com.stormpath.sdk.StormpathConfiguration;
        import com.stormpath.sdk.StormpathLogger;
        import android.app.Application;
/**
 * Created by emmakimlin on 2/28/17.
 */

public class ParkMatch extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            // we only want to show the logs in debug builds, for easier debugging
            Stormpath.setLogLevel(StormpathLogger.VERBOSE);
        }

        // initialize stormpath
        StormpathConfiguration stormpathConfiguration = new StormpathConfiguration.Builder()
                .baseUrl("https://striped-admiral.apps.stormpath.io")
                .build();
        Stormpath.init(this, stormpathConfiguration);
    }
};
