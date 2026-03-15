package controller.connectionMakerAndControlUnit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipInputStream;

import utility.Observer;
import utility.SysConstants;

/**
 * FetcherGTFS is responsible for downloading and storing GTFS (General Transit Feed Specification) 
 * data from a given URL. It implements Observer to allow update notifications triggering data fetch.
 * 
 * This class maintains a static list of all instances and ensures the GTFS data folder is created and 
 * cleaned up upon application shutdown.
 * 
 * <p>Note: This implementation assumes the GTFS data folder contains only files, no subdirectories.</p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class FetcherGTFS implements Observer {

	/**
     * The URL pointing to the GTFS data source.
     */
    private final URL referenceGTFS;
    
    /**
     * associated city
     */
    private final String city;

    /**
     * Location of the downloaded GTFS file (local path).
     */
    private final String locationOfFile;

    /**
     * Constant folder name for storing GTFS data files.
     */
    private static final String FOLDERGTFS = SysConstants.FOLDERGTFS;

    /**
     * Counter tracking how many FetcherGTFS objects have been created.
     */
    private static int allObjectsNumber = 0;

    /**
     * Unique ID for this FetcherGTFS instance.
     */
    private final int thisIdObject;
    
    
    /**
     * signals when the fetching operation has been completed
     */
    private boolean fetched;

    /**
     * Thread-safe list holding all FetcherGTFS instances.
     */
    private static final CopyOnWriteArrayList<FetcherGTFS> allFetchers = new CopyOnWriteArrayList<>();

	private static final int BUFFER_SIZE = 4096;

    /*
     * Static block to register a shutdown hook that deletes all files in the GTFS folder
     * when the application terminates.
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            deleteDirectory(new File(FOLDERGTFS));
        }));
    }

    /**
     * Constructs a FetcherGTFS with a specified URL.
     * Initializes internal state, generates file location, and creates folder if needed.
     * 
     * @param url the URL to fetch GTFS data from
     * @param city the city associated to the URL
     */
    public FetcherGTFS(URL url, String city) {
    		referenceGTFS = url;
    		this.city = city;
        this.thisIdObject = allObjectsNumber;
        allObjectsNumber++;
        this.locationOfFile = stringGenerator();
        fetched = false;
        allFetchers.add(this);
        if (thisIdObject == 0) {
            mkdirGTFS();
        }
        mkdirCity();
    }

    /**
     * Constructs a FetcherGTFS from a URI.
     * 
     * @param uri the URI pointing to GTFS data
     * @param city the city associated to the URL
     * @throws MalformedURLException if the URI is malformed
     */
    public FetcherGTFS(URI uri, String city) throws MalformedURLException {
        this(uri.toURL(), city);
    }

    /**
     * Constructs a FetcherGTFS from a String URL.
     * 
     * @param s string representing the GTFS URL
     * @param city the city associated to the URL
     * @throws MalformedURLException if the URL is malformed
     * @throws URISyntaxException if the string is not a valid URI
     */
    public FetcherGTFS(String s, String city) throws MalformedURLException, URISyntaxException {
        this(new URI(s), city);
    }

    /**
     * Triggered update from Observer interface.
     * Initiates fetching the GTFS data.
     */
    @Override
    public void update() {
        fetch();
    }

    /**
     * Returns an immutable copy of the list of all FetcherGTFS instances.
     * 
     * @return unmodifiable list of all FetcherGTFS objects
     */
    public static List<FetcherGTFS> getAllFetchers() {
        return List.copyOf(allFetchers);
    }
    
    /**
     * Returns a boolean expressing whether it has been checked
     * 
     * @return boolean hasBeenChecked
     */
    
    public boolean getHasBeenFetched() {
    		return fetched;
    }
    
    
    /**
     * Returns a String representing the city associated
     * 
     * @return a string representing the city
     */
    public String getCity() {
    		return city;
    }

    /**
     * Fetches the GTFS file from the URL if the connection is active.
     * Downloads the file and replaces the existing one if present.
     * then updates fetched to signal that the fetching operation has been completed
     */
    private void fetch() {
        if (MasterConnectionStatusChecker.getSingleton().isConnectionActive()) {
            try (var in = referenceGTFS.openStream()) {
                Files.copy(in, Paths.get(locationOfFile), StandardCopyOption.REPLACE_EXISTING);
                this.unzip();
                fetched = true;
            } catch (IOException e) {
                deleteDirectory(new File(SysConstants.CURRENTDIR + "/" + FOLDERGTFS + "/" + city));
            }
        }
    }

    /**
     * Generates a file path string for storing the GTFS file,
     * based on the folder and this instance's city.
     * 
     * @return the generated file path string
     */
    private String stringGenerator() {
        return FOLDERGTFS + File.separator + city + "/" + "_";
    }

    /**
     * Creates the GTFS folder in the current working directory if it does not already exist.
     */
    private void mkdirGTFS() {
        String currentDir = SysConstants.CURRENTDIR;
        var directoryFinal = currentDir + "/" + FOLDERGTFS;
        new File(directoryFinal).mkdir();
    }
    
    /**
     * Creates the city folder in the current working directory if it does not already exist.
     */
    private void mkdirCity() {
    	String currentDir = SysConstants.CURRENTDIR;
        var directoryFinal = currentDir + "/" + FOLDERGTFS + "/" + city;
        new File(directoryFinal).mkdirs();
        
    }

    
    /*
    /**
     * Deletes all files inside the specified folder.
     * Assumes the folder contains only files, no subdirectories.
     * 
     * @param folder the folder to clean
     *\/
    private static void deleteFilesInFolder(File folder) {
        if (folder.exists()) {
            var files = folder.listFiles();
            if (files != null) {
                for (var file : files) {
                    if (file != null && file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }
    */
    
    
    /**
     * Deletes all files and subdirectories inside the specified folder.
     * 
     * @param folder the folder to clean
     */
    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
        	File[] files = directory.listFiles();
            if (files != null) { 
                for (File file : files) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }
    

    /**
     * Estrae tutte le entry (file e cartelle) da un file ZIP in una directory di destinazione.
     *
     * @param zipFilePath    percorso completo del file .zip da estrarre
     * @param destDirectory  directory di destinazione (viene creata se non esiste)
     * @throws IOException   in caso di errori di I/O durante lettura o scrittura
     */
    
    private void unzip() throws FileNotFoundException, IOException {
    		try (var zipIn = new ZipInputStream(new FileInputStream(locationOfFile))){
    			var entry = zipIn.getNextEntry();
    			while (entry != null) {
    				String filePath = locationOfFile+entry.getName();
    				var fileExtracted = new File(filePath);
    				if (!fileExtracted.exists()) {
    					extractFile(zipIn, filePath);
    				} else {
    					fileExtracted.delete();
    					extractFile(zipIn, filePath);
    				}
    				zipIn.closeEntry();
    				entry = zipIn.getNextEntry();
    			}
    		}
    }
    
    
    /**
     * Estrae il contenuto di un singolo file all'interno dello Zip.
     *
     * @param zipIn     ZipInputStream già posizionato sull'entry da estrarre
     * @param filePath  percorso dove scrivere il file estratto
     * @throws IOException in caso di errori di I/O
     */
    
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        var outFile = new File(filePath);
        try (var fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            // leggi dallo zip e scrivi sul file
            while ((bytesRead = zipIn.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
    
    

    /**
     * Main method for basic demonstration and testing purposes.
     * Creates two FetcherGTFS instances and prints the number of fetchers.
     * 
     * @param args command-line arguments (not used)
     * @throws MalformedURLException if URLs are malformed
     * @throws URISyntaxException if string URLs are malformed
     */
    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
    		OfflineConnectionChecker.getSingleton().update();
    		MasterConnectionStatusChecker.getSingleton().update();
        var l2 = new FetcherGTFS("https://www.vbb.de/veroeffentlichungen/daten", "Berlin");
        l2.update();
        System.out.println(FetcherGTFS.getAllFetchers().size());
        while (true) {
        	try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}

