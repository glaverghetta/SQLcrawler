package usf.edu.bronie.sqlcrawler.io;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GDriveConnection {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String FOLDER_ID = "1O_6CgTGPdgFu5cyMa3CqKvEAX6-jwQdO";
    private static final String TMP_FILE_PATH =  CredentialConstants.RESOURCES + "tmp.java";

    private static final java.util.Collection<String> SCOPES = DriveScopes.all();
    private static final String CREDENTIALS_FILE_PATH = "client_secret.json";

    private static NetHttpTransport HTTP_TRANSPORT;

    private static Drive mDrive;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            mDrive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void uploadData(String code, String name) {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType("text/plain");
        fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

        try {
            PrintWriter out = new PrintWriter(TMP_FILE_PATH);
            out.print(code);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        File f = new File(TMP_FILE_PATH);
        FileContent mediaContent = new FileContent("text/plain", f);

        try {

            mDrive.files().create(fileMetadata, mediaContent).setFields("id").execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getFileByHashCode(String hash) {
        OutputStream outputStream = new ByteArrayOutputStream();

        try {
            FileList request = mDrive.files().list().setQ("name = '" + hash + ".java'").execute();
            String id = (String) request.getFiles().get(0).get("id");
            mDrive.files().get(id).executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(((ByteArrayOutputStream) outputStream).toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = getResourceAsStream();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    private static InputStream getResourceAsStream() {
        String file = CredentialConstants.RESOURCES + CREDENTIALS_FILE_PATH;
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
