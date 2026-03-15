package view;

import javax.swing.*;

import utility.CityTrack;

import java.util.List;



/**
 * Handles user interaction for selecting a city from the list of tracked cities.
 * <p>
 * This class presents a modal dialog allowing the user to choose among
 * predefined cities and sets the selection in the {@link CityTrack} utility.
 * </p>
 * <p>
 * If the user cancels the selection, the application exits.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class CitySelector {
	
	
    /**
     * Starts the city selection dialog.
     * <p>
     * Presents a sorted list of tracked cities and sets the selected city in {@link CityTrack}.
     * If the user cancels the selection, the application terminates immediately.
     * </p>
     */	
	public static void start() {
		var city = selectCity((CityTrack.TRACKEDCITIES.keySet().stream().sorted(
				(cityA, cityB) -> cityA.compareTo(cityB)).toList()));
        if (city == null) {
            System.exit(0);
        }
        
        CityTrack.setTrackedCity(city);
	}

    /**
     * Displays a modal dialog to prompt the user to select a city.
     * 
     * @param cityNames a non-null, possibly empty list of city names to choose from
     * @return the selected city name, or {@code null} if the user cancels or no cities are available
     */
    private static String selectCity(List<String> cityNames) {
        if (cityNames == null || cityNames.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No cities available to select.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        String[] cities = cityNames.toArray(new String[0]);
        return (String) JOptionPane.showInputDialog(
                null,
                "Select your city:",
                "City Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                cities,
                cities[0]
        );
    }
    
    
}