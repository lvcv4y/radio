package com.b_rap_radio.server.dataclasses.request_related;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JSONConverter {

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final Gson gson = new GsonBuilder().setDateFormat(dateFormat.toPattern())
			.registerTypeAdapter(Request.class, new RequestDeserializer()).create();

	public static Request getRequestFromJSON(@NotNull final String json) { return gson.fromJson(json, Request.class); }

	public static String getJSONFromRequest(@NotNull final Request request) { return gson.toJson(request); }

	private static class RequestDeserializer implements JsonDeserializer<Request> {

		@Override
		public Request deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			final JsonObject json = jsonElement.getAsJsonObject();

			String Rtype = null;
			boolean isError = false;
			String messageError = null;

			try {
				Rtype = json.get(Request.TYPE).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				isError = json.get(Request.IS_ERROR).getAsBoolean();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				if(!(json.get(Request.ERROR_MSG) instanceof JsonNull))
					messageError = json.get(Request.ERROR_MSG).getAsString();
			} catch (ClassCastException | IllegalStateException | NullPointerException ignored) { }

			Request result = new Request(Rtype, isError, messageError);

			if (json.has(Request.STR_ARGS) && !(json.get(Request.STR_ARGS) instanceof JsonNull)) {
				for (JsonElement element : json.getAsJsonArray(Request.STR_ARGS)) {
					if (element.getAsString() != null)
						result.addStringArgs(element.getAsString());
				}
			}

			if (json.has(Request.INT_ARGS) && !(json.get(Request.INT_ARGS) instanceof JsonNull)) {
				for (JsonElement element : json.getAsJsonArray(Request.INT_ARGS)) {
					result.addIntArgs(element.getAsInt());
				}
			}

			if (json.has(Request.EVENT_ARGS) && !(json.get(Request.EVENT_ARGS) instanceof JsonNull)) {
				for (JsonElement element : json.getAsJsonArray(Request.EVENT_ARGS)) {
					if (element.getAsJsonObject() != null) {
						result.addEventArgs(deserializeEvent(element.getAsJsonObject()));
					}
				}
			}

			if (json.has(Request.USER_ENTITY_ARGS) && !(json.get(Request.USER_ENTITY_ARGS) instanceof JsonNull)) {
				for (JsonElement element : json.getAsJsonArray(Request.USER_ENTITY_ARGS)) {
					if (element.getAsJsonObject() != null) {
						result.addUserArgs(deserializeUser(element.getAsJsonObject()));
					}
				}
			}

			if (json.has(Request.MUSIC_INFOS_ARGS) && !(json.get(Request.MUSIC_INFOS_ARGS) instanceof JsonNull)) {
				for (JsonElement element : json.getAsJsonArray(Request.MUSIC_INFOS_ARGS)) {
					if (element.getAsJsonObject() != null) {
						result.addMusicInfosArgs(deserializeMusicInfos(element.getAsJsonObject()));
					}
				}
			}

			if(json.has(Request.SANCTION_ENTITY_ARGS) && !(json.get(Request.SANCTION_ENTITY_ARGS) instanceof JsonNull)) {
				for (JsonElement element : json.getAsJsonArray(Request.SANCTION_ENTITY_ARGS)) {
					if(element.getAsJsonObject() != null){
						result.addSanctionArgs(deserializeSanction(element.getAsJsonObject()));
					}
				}
			}

			return result;
		}


		private MusicInfos deserializeMusicInfos(JsonObject JSONinfos) {
			String title = null;
			String albumName = null;
			String authors = null;
			String albumImageUrl = null;
			Integer duration = null;
			Integer totalVotes = null;
			Integer posVotes = null;
			Long playedPart = null;
			Long size = null;

			try {
				title = JSONinfos.get(MusicInfos.TITLE).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				albumName = JSONinfos.get(MusicInfos.ALBUM_NAME).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				authors = JSONinfos.get(MusicInfos.AUTHORS).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				albumImageUrl = JSONinfos.get(MusicInfos.ALBUM_IMAGE_URL).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				duration = JSONinfos.get(MusicInfos.DURATION).getAsInt();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				totalVotes = JSONinfos.get(MusicInfos.TOTAL_VOTE_NUM).getAsInt();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				posVotes = JSONinfos.get(MusicInfos.POSITIVE_VOTE_NUM).getAsInt();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				playedPart = JSONinfos.get(MusicInfos.PLAYED_PART).getAsLong();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				size = JSONinfos.get(MusicInfos.SIZE).getAsLong();
			} catch (ClassCastException | IllegalStateException ignored) { }

			return new MusicInfos(title, albumName, authors, albumImageUrl, duration, totalVotes, posVotes, size, playedPart);

		}


		// j'ai essayé avec une loop et un tableau, bah ca ramait du cul, a eviter
		private Event deserializeEvent(JsonObject JSONevent) {
			String title = null;
			String desc = null;
			Date startAt = null;
			Date endAt = null;

			try {
				title = JSONevent.get(Event.TITLE).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				desc = JSONevent.get(Event.DESC).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				startAt = dateFormat.parse(JSONevent.get(Event.START_AT).getAsString());
			} catch (ParseException | ClassCastException | IllegalStateException ignored) { }

			try {
				endAt = dateFormat.parse(JSONevent.get(Event.END_AT).getAsString());
			} catch (ParseException | ClassCastException | IllegalStateException ignored) { }

			return new Event(title, desc, startAt, endAt);
		}

		private UserEntity deserializeUser(JsonObject JSONuser) {

			Integer id = null;
			String pseudo = null;
			String email = null;
			List<String> status = new ArrayList<>();
			Integer credits = null;

			try {
				id = JSONuser.get(UserEntity.ID).getAsInt();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				pseudo = JSONuser.get(UserEntity.PSEUDO).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				email = JSONuser.get(UserEntity.EMAIL).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {

				for(JsonElement element : JSONuser.getAsJsonArray(UserEntity.STATUS)){
					status.add(element.getAsString());
				}

			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				credits = JSONuser.get(UserEntity.CREDITS).getAsInt();
			} catch (ClassCastException | IllegalStateException ignored) { }

			return new UserEntity(id, pseudo, email, status, credits);
		}

		private SanctionEntity deserializeSanction(JsonObject JSONsanction){

			Integer id = null;
			String type = null;
			UserEntity user = null;
			String desc = null;
			Date startAt = null;
			Date endAt = null;

			try {
				id = JSONsanction.get(SanctionEntity.ID).getAsInt();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				type = JSONsanction.get(SanctionEntity.TYPE).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				user = deserializeUser(JSONsanction.getAsJsonObject(SanctionEntity.USER));
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				desc = JSONsanction.get(SanctionEntity.DESC).getAsString();
			} catch (ClassCastException | IllegalStateException ignored) { }

			try {
				startAt = SanctionEntity.dateFormat.parse(JSONsanction.get(SanctionEntity.START_AT).getAsString());
			} catch (ClassCastException | IllegalStateException | ParseException ignored) { }

			try {
				endAt = SanctionEntity.dateFormat.parse(JSONsanction.get(SanctionEntity.END_AT).getAsString());
			} catch (ClassCastException | IllegalStateException | ParseException ignored) { }

			return new SanctionEntity(id, type, user, desc, startAt, endAt);
		}

	}
}