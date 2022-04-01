package com.b_rap_radio.server.dataclasses;

import com.b_rap_radio.server.dataclasses.request_related.MusicInfos;
import de.jarnbjo.ogg.FileStream;
import de.jarnbjo.ogg.LogicalOggStream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;

import javax.sound.sampled.UnsupportedAudioFileException;

public class Music {

	public static final int SAMPLES_RATE = 48000;
	public static final int FRAME_SIZE = 2880;
	public static final int CHANNELS = 2;


	public static int getDurationFromOggFile(File f) throws IOException {
		FileStream fs = new FileStream(new RandomAccessFile(f, "r"));
		long numOfPCMSamples = -1;

		for (LogicalOggStream stream : (Collection<LogicalOggStream>) fs.getLogicalStreams()) {
			// MaximumGranulePosition correspond to the number of PCM frame at 48kHz, so to get the duration of
			// the file, we just have to get this max granule position, and divide it by 48000
			if (stream.getMaximumGranulePosition() > 0) {

				numOfPCMSamples = numOfPCMSamples == -1
						? stream.getMaximumGranulePosition()
						: Math.min(numOfPCMSamples, stream.getMaximumGranulePosition());
			}
		}

		return (int) numOfPCMSamples * 4 - 1170;
	}


	
	public Music(String title, String albumName, String authors,File file, Long playedPart, String albumImageUrl)
			throws IOException, UnsupportedAudioFileException {
		this.title = title;
		this.albumName = albumName;
		this.authors = authors;
		this.playedPart = playedPart;
		this.file = file;

		FileStream fs = new FileStream(new RandomAccessFile(file, "r"));
		long numOfPCMSamples = -1;

		for (LogicalOggStream stream : (Collection<LogicalOggStream>) fs.getLogicalStreams()) {
			// MaximumGranulePosition correspond to the number of PCM frame at 48kHz, so to get the duration of
			// the file, we just have to get this max granule position, and divide it by 48000
			// the decoded file length is the numOfPCMSamples * stereo * 2 bytes wide, minus 1170 bytes (of headers ?)
			if (stream.getMaximumGranulePosition() > 0) {

				numOfPCMSamples = numOfPCMSamples == -1
						? stream.getMaximumGranulePosition()
						: Math.min(numOfPCMSamples, stream.getMaximumGranulePosition());
			}
		}

		this.fileSize = numOfPCMSamples * 4 - 1170;
		this.duration = (int) numOfPCMSamples / 48000;
		this.albumImageUrl = albumImageUrl;
	}
	
	synchronized public void increasePlayedPart(int i) { this.playedPart += i; }

	private final long fileSize;
	public long getSize() { return this.fileSize; }
	
	private long playedPart;
	synchronized public long getPlayedPart() { return this.playedPart; }
	synchronized public long getRemainPart() { return fileSize - playedPart; }
	synchronized public void resetPlayedPart(){ playedPart = 0L; }
	
	private final File file;
	public File getMusicFile() {return this.file; }
	
	private final String title;
	public String getTitle() { return this.title; }

	private final String albumName;
	public String getAlbumName() { return this.albumName; }

	private final int duration;
	public int getDuration() { return this.duration; }
	
	private final String authors;
	public String getAuthors() { return this.authors; }

	private final String albumImageUrl;
	public String getAlbumImageUrl() { return albumImageUrl; }

	public MusicInfos getInfos(int currentTotalVotes, int positiveVotes) {
		return new MusicInfos(title, albumName, authors, albumImageUrl, duration, currentTotalVotes, positiveVotes, fileSize, playedPart);
	}
}
