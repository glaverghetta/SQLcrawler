package usf.edu.bronie.sqlcrawler.constants;

import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class CredentialConstants {
    private static final Logger log = LogManager.getLogger(CredentialConstants.class);

    private static String GITHUB_TOKEN = null;
    private static String GITHUB_CACHE_FILE = null;

    // 10 MB cache by default.
    private static int GITHUB_CACHE_SIZE = 10 * 1024 * 1024;  

    private static String DB_URL = null;
    private static String DB_USER = null;
    private static String DB_PASS = null;

    private static String loadedConfigFile = null;

    public static String loadedConfigFile() {
        checkConfigLoaded();
        return loadedConfigFile;
    }

    public static String GITHUB_TOKEN() {
        checkConfigLoaded();
        return GITHUB_TOKEN;
    }

    public static String GITHUB_CACHE_FILE() {
        checkConfigLoaded();
        return GITHUB_CACHE_FILE;
    }

    public static int GITHUB_CACHE_SIZE() {
        checkConfigLoaded();
        return GITHUB_CACHE_SIZE;
    }

    public static String DB_URL() {
        checkConfigLoaded();
        return DB_URL;
    }

    public static String DB_USER() {
        checkConfigLoaded();
        return DB_USER;
    }

    public static String DB_PASS() {
        checkConfigLoaded();
        return DB_PASS;
    }

    private static void checkConfigLoaded(){
        if(loadedConfigFile == null){
            log.error("No config file loaded");
            System.exit(-1);
        }
    }

    public static void loadConfigFile(String file){
        CredentialConstants.loadedConfigFile = file;
        
        try{
            loadConf(new String(Files.readAllBytes(Paths.get(file))));
        }
        catch (Exception e){
            log.error("Error reading config file: ", e);
            System.exit(-1);
        }
    }    

    public static void loadConfigResource(String file){
        CredentialConstants.loadedConfigFile = file;

        InputStream input = CredentialConstants.class.getResourceAsStream(file);
        try{
            loadConf(new String(input.readAllBytes()));
        }
        catch (Exception e){
            log.error("Error reading config file: ", e);
            System.exit(-1);
        }
    }

    private static void loadConf(String in){
        try{
            JSONObject a = new JSONObject(in);
            
            CredentialConstants.GITHUB_TOKEN = a.getString("GITHUB_TOKEN");
            CredentialConstants.GITHUB_CACHE_FILE = a.getString("GITHUB_CACHE_FILE");
            CredentialConstants.GITHUB_CACHE_SIZE = a.getInt("GITHUB_CACHE_SIZE");
            CredentialConstants.DB_URL = a.getString("DB_URL");
            CredentialConstants.DB_USER = a.getString("DB_USER");
            CredentialConstants.DB_PASS = a.getString("DB_PASS");

        }
        catch (Exception e){
            log.error("Error reading config file: ", e);
            System.exit(-1);
        }
    }

}
