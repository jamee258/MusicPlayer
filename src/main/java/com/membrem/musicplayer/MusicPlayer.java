package com.membrem.musicplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MusicPlayer extends Application {

	private static String fileName = "C:\\Users\\ISLAMM\\Documents\\Music Player MP3 Files";

	public static void main(String[] args) throws Exception {

		File[] listOfFiles = getAllFiles(fileName);

		deleteDBRecords();

		for (File pathname : listOfFiles) {

			Metadata metadataDetails = retrieveMetadataDetails(pathname);

			String convertedDuration = convertDuration(metadataDetails);

			String trackTitle = metadataDetails.get("title");
			String artist = metadataDetails.get("xmpDM:artist");
			String album = metadataDetails.get("xmpDM:album");
			String genre = metadataDetails.get("xmpDM:genre");

			// Prevent null values being inserted into DB
			if (album == null) {
				album = "N/A";
			}

			if (genre == null) {
				genre = "N/A";
			}

			insertIntoDB(trackTitle, artist, album, genre, pathname, convertedDuration);
			retrieveAllTracks();
		}

		launch(args);
	}

	public static ResultSet retrieveAllTracks() {
		String SQLSelect = "SELECT * FROM MusicTrack";
		ResultSet tracks = null;
		Connection dbConn = connectToDB();

		try {
			Statement musicTrackStatement = dbConn.createStatement();
			tracks = musicTrackStatement.executeQuery(SQLSelect);

			// Iterate through the data in the result set and display it.
			while (tracks.next()) {
				System.out.println(tracks.getString(1) + " " + tracks.getString(2));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tracks;

	}

	private static void deleteDBRecords() {
		String deleteQuery = "DELETE FROM MusicTrack";

		Connection dbConn = connectToDB();

		try {
			Statement deleteStatement = dbConn.createStatement();
			deleteStatement.executeUpdate(deleteQuery);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void insertIntoDB(String trackTitle, String artist, String album, String genre, File pathname,
			String duration) {

		Connection dbConn = connectToDB();

		try {
			Statement insertStatement = dbConn.createStatement();
			insertStatement.executeUpdate("INSERT INTO MusicTrack VALUES ('" + trackTitle + "','" + artist + "', '"
					+ album + "', '" + genre + "', '" + pathname + "', '" + duration + "')");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void start(Stage musicLibraryStage) {
		try {
			// Load the Music Library FXML file
			Parent musicLibraryRoot = FXMLLoader
					.load(MusicPlayer.class.getClassLoader().getResource("MusicLibrary.fxml"));
			Scene libraryScene = new Scene(musicLibraryRoot);
			libraryScene.getStylesheets()
					.add(MusicPlayer.class.getClassLoader().getResource("application.css").toExternalForm());
			Image appIcon = new Image(MusicPlayer.class.getClassLoader().getResourceAsStream("musicplayerlogo.jpg"));
			musicLibraryStage.getIcons().add(appIcon);
			musicLibraryStage.setTitle("Rebmem Engineering Music Player");
			musicLibraryStage.setScene(libraryScene);
			musicLibraryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File[] getAllFiles(String directory) {
		// Populate array with file names and directories
		File[] pathNames;

		File audioFile = new File(directory);

		pathNames = audioFile.listFiles();

		return pathNames;
	}

	public static Metadata retrieveMetadataDetails(File directory) throws IOException, SAXException, TikaException {
		InputStream input = new FileInputStream(directory);
		ContentHandler handler = new DefaultHandler();
		Metadata audioMetadata = new Metadata();
		Parser parser = new Mp3Parser();
		ParseContext parseCtx = new ParseContext();
		parser.parse(input, handler, audioMetadata, parseCtx);
		input.close();
		return audioMetadata;
	}

	public static String convertDuration(Metadata durationMetadata) {
		// Get meta data duration of track and convert to a more readable format
		String duration = durationMetadata.get("xmpDM:duration");

		Long milliSeconds = (long) Double.parseDouble(duration);

		// Convert milliseconds into a more readable format
		String hoursMinutesSeconds = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
				TimeUnit.MILLISECONDS.toMinutes(milliSeconds)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
				TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

		return hoursMinutesSeconds;
	}

	public static Connection connectToDB() {
		Connection con = null;
		try {
			// Build connection string for SQL connection
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionURL = "jdbc:sqlserver://localhost:1433;" + "database=MusicPlayer;" + "user=user2;"
					+ "password=Security123!";
			con = DriverManager.getConnection(connectionURL);
			System.out.println("Connected to Database");

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}

		return con;
	}
}