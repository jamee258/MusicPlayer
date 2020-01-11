package com.rembrem.musicplayer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.xml.sax.SAXException;

import com.membrem.musicplayer.MusicLibraryFXMLController;
import com.membrem.musicplayer.MusicPlayer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MusicLibraryTest extends ApplicationTest {

	@Before
	public void setUpMediaPlayer() throws Exception {
		ApplicationTest.launch(MusicPlayer.class);
	}

	@Override
	public void start(Stage musicLibraryStage) {
		try {
			// Load the FXML file
			Parent root = FXMLLoader.load(MusicPlayer.class.getClassLoader().getResource("MusicLibrary.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets()
					.add(MusicPlayer.class.getClassLoader().getResource("application.css").toExternalForm());
			musicLibraryStage.setScene(scene);
			musicLibraryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSpecificDataRetrieval() throws SQLException {
		int rows = 0;
		ResultSet specificTracks = MusicLibraryFXMLController.retrieveSpecificTracks("Fur Elise");
		while (specificTracks.next()) {
			++rows;
		}

		assertEquals(1, rows);
	}

	@Test
	public void testMetadataRetrieval() throws IOException, SAXException, TikaException {
		String audioFilePath = "C:\\Users\\ISLAMM\\Documents\\Music Player MP3 Files\\fur-elise.mp3";
		String editedFilePath = audioFilePath.replace("\\", "/");
		File mediaFile = new File(editedFilePath);
		Metadata metadata = MusicPlayer.retrieveMetadataDetails(mediaFile);
		String artist = metadata.get("xmpDM:artist");

		assertEquals("Ludwig van Beethoven", artist);
	}

}