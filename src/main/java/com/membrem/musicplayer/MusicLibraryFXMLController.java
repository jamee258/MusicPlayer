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
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MusicLibraryFXMLController {

	@FXML
	private Pane mainPane;

	@FXML
	public Pane tracksPane;

	@FXML
	private Text trackNameText;

	@FXML
	private ImageView trackLogo;

	@FXML
	private Pane trackRow;

	@FXML
	private Button musicPlayerButton;

	@FXML
	private Button musicLibraryButton;

	@FXML
	public TableView<Track> tracksTable;

	@FXML
	public TableColumn<Track, String> artistColumn;

	@FXML
	public TableColumn<Track, String> trackNameColumn;

	@FXML
	public TableColumn<Track, String> albumNameColumn;

	@FXML
	private Button refreshTracksButton;

	@FXML
	private Button playButton;

	@FXML
	private TextField trackSearchText;

	@FXML
	private Button trackSearchButton;

	@FXML
	private Button createPlaylistButton;

	ArrayList<Track> musicPlayerTrackList = new ArrayList<Track>();

	@FXML
	public void initialize() throws SQLException {
		ObservableList<Track> tracksList = FXCollections.observableArrayList();
		artistColumn.setCellValueFactory(new PropertyValueFactory<Track, String>("Artist"));
		trackNameColumn.setCellValueFactory(new PropertyValueFactory<Track, String>("TrackName"));
		albumNameColumn.setCellValueFactory(new PropertyValueFactory<Track, String>("Album"));

		ResultSet tracks = retrieveAllTracks();

		while (tracks.next()) {
			Track track = new Track();
			track.setTrackName(tracks.getString(2));
			track.setArtist(tracks.getString(3));
			track.setAlbum(tracks.getString(4));
			track.setGenre(tracks.getString(5));
			track.setFileName(tracks.getString(6));
			track.setDuration(tracks.getString(7));
			tracksList.add(track);
			musicPlayerTrackList.add(track);
			tracksTable.setItems(tracksList);
		}

	}

	public void retrieveSpecificTracks(MouseEvent event) throws SQLException {
		ObservableList<Track> tracksList = FXCollections.observableArrayList();
		artistColumn.setCellValueFactory(new PropertyValueFactory<Track, String>("Artist"));
		trackNameColumn.setCellValueFactory(new PropertyValueFactory<Track, String>("TrackName"));
		String trackName = trackSearchText.getText();
		ResultSet tracks = retrieveSpecificTracks(trackName);

		while (tracks.next()) {
			Track track = new Track();
			track.setArtist(tracks.getString(3));
			track.setTrackName(tracks.getString(2));
			track.setFileName(tracks.getString(6));
			tracksList.add(track);
			tracksTable.setItems(tracksList);
		}
	}

	// Populate ArrayList in play list FXML controller
	public void addToPlaylist(MouseEvent event) throws IOException {
		Track selectedtrack = tracksTable.getSelectionModel().getSelectedItem();
		FXMLLoader loader = new FXMLLoader(MusicPlayer.class.getClassLoader().getResource("MusicPlaylist.fxml"));
		@SuppressWarnings("unused")
		Parent playlistRoot = loader.load();
		MusicPlaylistFXMLController musicPlaylistController = loader.getController();
		if (selectedtrack != null) {
			musicPlaylistController.populatePlaylist(selectedtrack);
		}
	}

	public void changeScreenButtonPressed(MouseEvent action) throws IOException {
		Parent musicPlaylistRoot = FXMLLoader
				.load(MusicPlayer.class.getClassLoader().getResource("MusicPlaylist.fxml"));
		Scene musicPlaylistScene = new Scene(musicPlaylistRoot);

		Stage musicPlaylistWindow = (Stage) ((Node) action.getSource()).getScene().getWindow();
		musicPlaylistWindow.setScene(musicPlaylistScene);
		musicPlaylistWindow.show();
	}

	// Pass selected track details to the Music Player screen and play audio
	public void changeSceneToMusicPlayerScreen(MouseEvent event) throws IOException, SAXException, TikaException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MusicPlayer.class.getClassLoader().getResource("MusicPlayer.fxml"));
		Parent musicPlayerParent = loader.load();

		Scene musicPlayerScene = new Scene(musicPlayerParent);

		MusicPlayerFXMLController musicPlayerController = loader.getController();
		musicPlayerController.fillScreenData(tracksTable.getSelectionModel().getSelectedItem());

		// Populate Music Player ArrayList
		for (Track track : musicPlayerTrackList) {
			musicPlayerController.fillTracksList(track);
		}

		String filepath = (tracksTable.getSelectionModel().getSelectedItem().getFileName());
		String editedFilePath = filepath.replace("\\", "/");
		File chosenFile = new File(editedFilePath);
		Metadata fileMetadata = retrieveMetadataDetails(chosenFile);
		String convertedDuration = convertDuration(fileMetadata);
		musicPlayerController.setTrackDuration(convertedDuration);
		MusicPlayerFXMLController.playFile(chosenFile);
		musicPlayerController.initialiseTrackSlider();

		Stage musicPlayerWindow = (Stage) ((Node) event.getSource()).getScene().getWindow();
		musicPlayerWindow.setScene(musicPlayerScene);
		musicPlayerWindow.show();
	}

	public void changeSceneToPlaylist(MouseEvent event) throws IOException {
		Parent musicPlaylistRoot = FXMLLoader
				.load(MusicPlayer.class.getClassLoader().getResource("MusicPlaylist.fxml"));
		Scene musicPlaylistScene = new Scene(musicPlaylistRoot);

		Stage musicPlaylistStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		musicPlaylistStage.setScene(musicPlaylistScene);
		musicPlaylistStage.show();
	}

	public void calculateNumberOfTracks() {
		ResultSet rs2 = retrieveAllTracks();
		try {
			this.tracksPane.getChildren().clear();
			GridPane gridpane = new GridPane();
			this.tracksPane.getChildren().add(gridpane);

			int rowNumber = 1;
			while (rs2.next()) {

				Rectangle r3 = new Rectangle();
				r3.setWidth(333);
				r3.setHeight(70);
				r3.setFill(Color.BISQUE);

				// On mouse click of rectangle, navigate to Music Player screen, passing the
				// track details
				r3.setOnMouseClicked(mouseEvent -> {
					Parent musicPlayerRoot;
					try {
						musicPlayerRoot = FXMLLoader
								.load(MusicPlayer.class.getClassLoader().getResource("MusicPlayer.fxml"));
						Scene musicPlayerScene = new Scene(musicPlayerRoot);
						Stage musicPlayerWindow = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
						musicPlayerWindow.setScene(musicPlayerScene);
						musicPlayerWindow.show();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

				StackPane sp1 = new StackPane();
				sp1.setMaxWidth(333);
				sp1.setMaxHeight(70);
				sp1.setPadding(new Insets(0, 0, 10, 0));

				Text text = new Text(rs2.getString(3) + " - " + rs2.getString(2));

				sp1.getChildren().addAll(r3, text);
				gridpane.add(sp1, 1, rowNumber);
				rowNumber++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ResultSet retrieveAllTracks() {
		String SQLSelect = "SELECT * FROM MusicTrack";
		ResultSet tracks = null;
		Connection dbConn = connectToDB();

		try {
			Statement stmt = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			tracks = stmt.executeQuery(SQLSelect);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tracks;
	}

	public static ResultSet retrieveSpecificTracks(String trackName) {
		String SQLSelect = "SELECT * FROM MusicTrack WHERE TrackName LIKE '%" + trackName + "%'";
		ResultSet tracks = null;
		Connection dbConn = connectToDB();

		try {
			Statement stmt = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			tracks = stmt.executeQuery(SQLSelect);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tracks;
	}

	public static Connection connectToDB() {
		Connection con = null;
		try {
			// Load the SQLServerDriver class, build the connection string, and get a
			// connection
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://localhost:1433;" + "database=MusicPlayer;" + "user=user2;"
					+ "password=Security123!";
			con = DriverManager.getConnection(connectionUrl);
			System.out.println("Connected to Database");

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}

		return con;
	}

	public static Metadata retrieveMetadataDetails(File directory) throws IOException, SAXException, TikaException {
		InputStream input = new FileInputStream(directory);
		ContentHandler handler = new DefaultHandler();
		Metadata metadata = new Metadata();
		Parser parser = new Mp3Parser();
		ParseContext parseCtx = new ParseContext();
		parser.parse(input, handler, metadata, parseCtx);
		input.close();
		return metadata;
	}

	public static String convertDuration(Metadata durationMetadata) {
		// Get duration of track and convert to long
		String duration = durationMetadata.get("xmpDM:duration");

		Long milliSeconds = (long) Double.parseDouble(duration);

		// Convert milliseconds into a more readable format
		String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
				TimeUnit.MILLISECONDS.toMinutes(milliSeconds)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
				TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

		return hms;
	}
}