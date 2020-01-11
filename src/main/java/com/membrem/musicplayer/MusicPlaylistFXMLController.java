package com.membrem.musicplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class MusicPlaylistFXMLController {

	@FXML
	private Pane mainPane;

	@FXML
	private Button addTrackButton;

	@FXML
	private Button playButton;

	@FXML
	private TableView<Track> playlistTable;

	@FXML
	private TableColumn<Track, String> artistColumn;

	@FXML
	private TableColumn<Track, String> trackNameColumn;

	@FXML
	private Button playlistMenuButton;

	@FXML
	private Button musicLibraryMenuButton;

	private static ArrayList<Track> playlist = new ArrayList<Track>();

	@FXML
	public void initialize() {
		ObservableList<Track> tracksPlaylist = FXCollections.observableArrayList();
		artistColumn.setCellValueFactory(new PropertyValueFactory<Track, String>("Artist"));
		trackNameColumn.setCellValueFactory(new PropertyValueFactory<Track, String>("TrackName"));

		for (Track t : playlist) {
			tracksPlaylist.add(t);
			playlistTable.setItems(tracksPlaylist);
		}
	}

	// Method for selecting file from system explorer
	@FXML
	public void openFileBrowser(MouseEvent mouseEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose An MP3 File To Add To Playlist");
		// Only display mp3 files
		fileChooser.getExtensionFilters().add(new ExtensionFilter("MP3 Format Sound", "*.mp3"));
		@SuppressWarnings("unused")
		File chosenFile = fileChooser.showOpenDialog((Stage) ((Node) mouseEvent.getSource()).getScene().getWindow());
		// The chosen file would have to be converted to a track before adding to
		// play list
	}

	public void populatePlaylist(Track selectedTrack) {
		boolean trackExists = false;
		// First check if a track with same filename already exists in play list
		for (Track track : playlist) {
			if (track.getTrackName().equals(selectedTrack.getTrackName())) {
				trackExists = true;
			}
		}

		if (trackExists == false) {
			playlist.add(selectedTrack);
		} else {
			System.out.println("Same track already exists in this playlist");
		}
	}

	// Change to Music Library screen when button is pressed.
	public void changeScreenToMusicLibrary(MouseEvent action) throws IOException {
		Parent musicLibraryRoot = FXMLLoader.load(MusicPlayer.class.getClassLoader().getResource("MusicLibrary.fxml"));
		Scene musicLibraryScene = new Scene(musicLibraryRoot);

		Stage musicPlayerWindow = (Stage) ((Node) action.getSource()).getScene().getWindow();
		musicPlayerWindow.setScene(musicLibraryScene);
		musicPlayerWindow.show();
	}

}