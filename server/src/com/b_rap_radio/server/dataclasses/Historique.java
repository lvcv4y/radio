package com.b_rap_radio.server.dataclasses;

import com.b_rap_radio.server.admin_server.TCPAdminHandler;
import com.b_rap_radio.server.dataclasses.request_related.MusicInfos;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;

import java.util.ArrayList;
import java.util.List;

public class Historique {
	public static final int SIZE = 10;

	private static Historique instance;

	public static Historique getInstance() {
		if(instance == null) instance = new Historique(SIZE);
		return instance;
	}
	
	public Historique(int size) {
		this.historiqueArray = new ArrayList<>();
		this.size = size;
	}
	
	synchronized public void add(MusicInfos music) {
		this.historiqueArray.add(0, music);
		if(this.historiqueArray.size() > size)
			this.historiqueArray.remove(size);

		final Request res = new Request(RequestTypes.ADD_HISTORY.getName());
		res.addMusicInfosArgs(music);
		new Thread(new TCPAdminHandler.GeneralQuery(res, false, null));
	}

	synchronized public boolean isInHistorique(MusicInfos music) {

		for(MusicInfos infos : historiqueArray){
			if(infos.getTitle().equals(music.getTitle())){
				if(infos.getAlbumName().equals(music.getAlbumName())) return true;
			}
		}

		return false;
	}
	
	synchronized public MusicInfos getMusicInfos(int id) {
		try {
			return historiqueArray.get(id);
		} catch (IndexOutOfBoundsException e) { return null; }
	}

	synchronized public int getSize(){ return historiqueArray.size(); }

	synchronized public List<MusicInfos> getHistorique(){ return historiqueArray; }
	
	private final ArrayList<MusicInfos> historiqueArray;
	private final int size;
}
