package knightwing.ws.weedspotter.Models.PlantIdentification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Used to request the location of the device using GPS.
 */
public class LocationService extends Service {
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
