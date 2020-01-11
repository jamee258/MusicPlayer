package com.membrem.musicplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.tika.exception.TikaException;

import org.xml.sax.SAXException;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MusicPlayerFXMLController {

	private Track chosenTrack;

	@FXML
	private Text artistAndTrackName;

	@FXML
	private Button musicLibraryMenuButton;

	@FXML
	private Text trackDuration;

	@FXML
	private Text currentTrackPosition;

	@FXML
	private Slider trackSlider;

	@FXML
	Button playButton;

	@FXML
	Button pauseButton;

	@FXML
	Button stopButton;

	@FXML
	Button shuffleButton;

	static ArrayList<Track> tracksList = new ArrayList<Track>();

	static Iterator<Track> tracksIterator;
	static Iterator<Track> shuffledTracksIterator;

	public void fillTracksList(Track track) {
		tracksList.add(track);
	}

	static MediaPlayer mediaPlayer;
	static boolean mpTracker = false;
	protected Duration duration;

	@FXML
	public void initialize() {
		trackSlider.valueProperty().addListener((observable, oldValue, newValue) -> {

			currentTrackPosition.setText(Double.toString(newValue.intValue()));

		});
	}

	public void moveTrackSlider() {
		@SuppressWarnings("unused")
		double sliderWidth = trackSlider.getWidth();
	}

	// TODO Revisit slider functionality
	public void initialiseTrackSlider() {
		trackSlider.valueProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable observable) {
				if (trackSlider.isValueChanging()) {
					mediaPlayer.seek(duration.multiply(trackSlider.getValue() / 100.0));
				}
			}

		});
	}

	private void updateSliderValues() {
		if (currentTrackPosition != null && trackSlider != null) {

			trackSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> obs, Boolean wasChanging,
						Boolean isNowChanging) {
					if (!isNowChanging) {
						mediaPlayer.seek(Duration.seconds(trackSlider.getValue()));
					}
				}
			});

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					Duration currentTrackTime = mediaPlayer.getCurrentTime();
					String convertedCurrentTrackTime = convertDuration(currentTrackTime);
					currentTrackPosition.setText(convertedCurrentTrackTime);
				}

			});
		}
	}

	// Populate screen fields with track information
	public void fillScreenData(Track track) {
		chosenTrack = track;
		artistAndTrackName.setText(chosenTrack.getArtist() + " - " + chosenTrack.getTrackName());
	}

	// Change screen when button is pressed.
	public void changeScreenToPlaylistScreen(MouseEvent action) throws IOException {
		Parent musicPlaylistRoot = FXMLLoader
				.load(MusicPlayer.class.getClassLoader().getResource("MusicPlaylist.fxml"));
		Scene musicPlaylistScene = new Scene(musicPlaylistRoot);

		Stage musicPlaylistWindow = (Stage) ((Node) action.getSource()).getScene().getWindow();
		musicPlaylistWindow.setScene(musicPlaylistScene);
		musicPlaylistWindow.show();
	}

	// Change to Music Library screen when button is pressed.
	public void changeScreenToMusicLibrary(MouseEvent action) throws IOException {
		Parent musicLibraryRoot = FXMLLoader.load(MusicPlayer.class.getClassLoader().getResource("MusicLibrary.fxml"));
		Scene musicLibraryScene = new Scene(musicLibraryRoot);

		Stage musicLibraryWindow = (Stage) ((Node) action.getSource()).getScene().getWindow();
		musicLibraryWindow.setScene(musicLibraryScene);
		musicLibraryWindow.show();
	}

	public void play(ActionEvent event) {
		System.out.println("Duration of media = " + duration);
		if (mediaPlayer.getStatus().equals(Status.PAUSED)) {
			mediaPlayer.play();
			updateSliderValues();
		} else if (mediaPlayer.getStatus().equals(Status.STOPPED)) {
			mediaPlayer.play();
		} else if (mediaPlayer.getStatus().equals(Status.PLAYING)) {
			System.out.println("Media Player is already playing...");
		}
	}

	@FXML
	public void pause(ActionEvent event) {
		if (mediaPlayer.getStatus().equals(Status.PLAYING)) {
			Duration currentPosition = mediaPlayer.getCurrentTime();
			System.out.println("Current Position: " + currentPosition);
			mediaPlayer.pause();
		}
	}

	@FXML
	public void stop(ActionEvent event) {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}

	@FXML
	public void nextTrack() {

	}

	@FXML
	public void previousTrack() {

	}

	// Play a track from the Music Library page
	public static void playFile(File file) throws IOException, SAXException, TikaException {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.dispose();
		}

		// Using the MediaPlayer class to play the mp3 file
		Media mp3File = new Media(file.toURI().toString());
		mediaPlayer = new MediaPlayer(mp3File);

		mediaPlayer.setOnReady(() -> {
			mediaPlayer.play();
			mpTracker = true;
			mediaPlayer.setOnEndOfMedia(() -> {
				mediaPlayer.dispose();

				// Start playing tracks from beginning of ArrayList
				tracksIterator = tracksList.iterator();
				if (tracksIterator.hasNext()) {
					String editedFilePath = tracksIterator.next().getFileName().replace("\\", "/");
					File nextFile = new File(editedFilePath);
					Media nextMP3File = new Media(nextFile.toURI().toString());
					mediaPlayer = new MediaPlayer(nextMP3File);
					mediaPlayer.play();
				}
			});
		});
	}

	public void shuffle(MouseEvent event) {
		ArrayList<Track> shuffledList = new ArrayList<Track>();
		for (Track track : tracksList) {
			System.out.println(track.getTrackName());
			shuffledList.add(track);
		}
		Collections.shuffle(shuffledList);
		shuffledTracksIterator = shuffledList.iterator();

		if (shuffledTracksIterator.hasNext()) {
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.dispose();
			}
			String editedFilePath = shuffledTracksIterator.next().getFileName().replace("\\", "/");
			File file = new File(editedFilePath);
			Media mp3File = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(mp3File);
			mediaPlayer.play();
		}
	}

	public void setTrackDuration(String duration) {
		trackDuration.setText(duration);
	}

	public static String convertDuration(Duration duration) {
		// Get duration of track and convert to long
		Long milliSeconds = (long) duration.toMillis();

		// Convert milliseconds into a more readable format
		String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
				TimeUnit.MILLISECONDS.toMinutes(milliSeconds)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
				TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

		return hms;
	}

}