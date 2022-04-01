package brap.radio.brap_radio.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class JSONConverter {
    public static final Gson gson = new GsonBuilder().registerTypeAdapter(Request.class, new RequestDeserializer()).create();

    public static Request getRequestFromJSON(final String json){
        return gson.fromJson(json, Request.class);
    }

    public static String getJSONFromRequest(final Request request){
        return gson.toJson(request);
    }

    private static class RequestDeserializer implements JsonDeserializer<Request> {

        @Override
        public Request deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            final JsonObject json = jsonElement.getAsJsonObject();

            String Rtype = null;
            boolean isError = false;
            String messageError = null;

            try {
                Rtype = json.get(Request.TYPE).getAsString();
            } catch(ClassCastException | IllegalStateException ignored){}

            if(!"PLAYING".equals(Rtype))
                return null;

            try {
                isError = json.get(Request.IS_ERROR).getAsBoolean();
            } catch(ClassCastException | IllegalStateException ignored){}

            if(isError){
                try {
                    messageError = json.get(Request.ERROR_MSG).getAsString();
                } catch(ClassCastException | IllegalStateException ignored){}
            }

            Request result = new Request(Rtype, isError, messageError);

            if(json.has(Request.STR_ARGS)){
                for (JsonElement element : json.getAsJsonArray(Request.STR_ARGS)) {
                    if(element.getAsString() != null)
                        result.addStringArgs(element.getAsString());
                }
            }

            if(json.has(Request.MUSIC_INFOS_ARGS)){
                for (JsonElement element : json.getAsJsonArray(Request.MUSIC_INFOS_ARGS)) {
                    if(element.getAsJsonObject() != null){
                        result.addMusicInfosArgs(deserializeMusicInfos(element.getAsJsonObject()));
                    }
                }
            }


            return result;
        }


        private MusicInfos deserializeMusicInfos(JsonObject JSONinfos){
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
            } catch(ClassCastException | IllegalStateException ignored){}

            try {
                albumName = JSONinfos.get(MusicInfos.ALBUM_NAME).getAsString();
            } catch(ClassCastException | IllegalStateException ignored){}

            try {
                authors = JSONinfos.get(MusicInfos.AUTHORS).getAsString();
            } catch(ClassCastException | IllegalStateException ignored){}

            try {
                albumImageUrl = JSONinfos.get(MusicInfos.ALBUM_IMG).getAsString();
            } catch(ClassCastException | IllegalStateException ignored){}

            try {
                duration  = JSONinfos.get(MusicInfos.DURATION).getAsInt();
            } catch(ClassCastException | IllegalStateException ignored){}

            try {
                totalVotes = JSONinfos.get(MusicInfos.TOTAL_VOTE_NUM).getAsInt();
            } catch(ClassCastException | IllegalStateException ignored){}

            try {
                posVotes = JSONinfos.get(MusicInfos.POSITIVE_VOTE_NUM).getAsInt();
            } catch(ClassCastException | IllegalStateException ignored){}

            try {
                playedPart = JSONinfos.get(MusicInfos.PLAYED_PART).getAsLong();
            } catch(ClassCastException | IllegalStateException ignored){}

            try {
                size  = JSONinfos.get(MusicInfos.SIZE).getAsLong();
            } catch(ClassCastException | IllegalStateException ignored){}

            return new MusicInfos(title, albumName, authors, albumImageUrl, duration, totalVotes, posVotes, size, playedPart);

        }
    }
}
