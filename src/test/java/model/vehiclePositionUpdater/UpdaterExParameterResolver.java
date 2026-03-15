package model.vehiclePositionUpdater;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.extension.*;

import controller.connectionMakerAndControlUnit.FetcherGTFS_RT;
import model.vehiclePositionUpdater.UpdaterTest.UpdaterEx;
import model.vehicles.Manager;
import utility.CityTrack;
import utility.exceptionUtils.DuplicationNotAccepted;
import utility.exceptionUtils.FetcherGTFS_RTNonExistent;

public class UpdaterExParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == UpdaterEx.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext){
        try {
            var allCityManagers = Manager.class.getDeclaredField("allCityManagers");
            allCityManagers.setAccessible(true);
            List<?> list = (List<?>) allCityManagers.get(null);
            list.clear(); // clear the static map directly
            
            var cities = Manager.class.getDeclaredField("cities");
            cities.setAccessible(true);
            List<?> list1 = (List<?>) cities.get(null);
            list1.clear();

            var fetcherGTFS_RT = new FetcherGTFS_RT(CityTrack.TRACKEDCITIES.get("Rome").getY(), "Rome");
            return new UpdaterEx("Rome");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (DuplicationNotAccepted e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FetcherGTFS_RTNonExistent e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return extensionContext;
    }
}