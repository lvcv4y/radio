package com.b_rap_radio.server.dataclasses.request_related;

import com.google.gson.annotations.SerializedName;

public class MusicInfos {

	public static final String TITLE = "title";
	public static final String ALBUM_NAME = "album_name";
	public static final String AUTHORS = "authors";
	public static final String SIZE = "size";
	public static final String DURATION = "duration";
	public static final String PLAYED_PART = "played_part";
	public static final String ALBUM_IMAGE_URL = "album_image_url";
	public static final String TOTAL_VOTE_NUM = "total_vote_num";
	public static final String POSITIVE_VOTE_NUM = "positive_vote_num";

	public MusicInfos(String title, String albumName, String authors, String albumImageUrl, Integer duration,
					  Integer totalVoteNumber, Integer positiveVoteNumber, Long size, Long playedPart){
		this.title = title;
		this.albumName = albumName;
		this.authors = authors;
		this.size = size;
		this.duration = duration;
		this.playedPart = playedPart;
		this.positiveVoteNumber = positiveVoteNumber;
		this.totalVoteNumber = totalVoteNumber;
		this.albumImageUrl = albumImageUrl;
	}


	@SerializedName(TITLE)
	private final String title;
	public String getTitle() { return this.title; }

	@SerializedName(AUTHORS)
	private final String authors;
	public String getAuthors() { return this.authors; }

	@SerializedName(ALBUM_NAME)
	private final String albumName;
	public String getAlbumName() { return this.albumName; }

	@SerializedName(SIZE)
	private final Long size;
	public Long getSize() { return this.size; }

	@SerializedName(DURATION)
	private final Integer duration;
	public int getDuration() { return this.duration; }

	@SerializedName(PLAYED_PART)
	private final Long playedPart;
	public Long getPlayedPart() { return this.playedPart; }

	@SerializedName(ALBUM_IMAGE_URL)
	private final String albumImageUrl;
	public String getAlbumImageUrl() { return this.albumImageUrl; }

	@SerializedName(TOTAL_VOTE_NUM)
	private final Integer totalVoteNumber;
	public int getTotalVoteNumber(){return this.totalVoteNumber; }

	@SerializedName(POSITIVE_VOTE_NUM)
	private final Integer positiveVoteNumber;
	public int getPositiveVoteNumber() { return this.positiveVoteNumber; }
}