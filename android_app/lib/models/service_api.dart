import 'dart:async';
import 'dart:convert';

import 'package:brap_radio/cubits/chat_cubit.dart';
import 'package:brap_radio/cubits/connection_cubit.dart';
import 'package:brap_radio/cubits/event_cubit.dart';
import 'package:brap_radio/cubits/history_cubit.dart';
import 'package:brap_radio/cubits/mute_cubit.dart';
import 'package:brap_radio/cubits/playing_cubit.dart';
import 'package:brap_radio/cubits/playing_progress_cubit.dart';
import 'package:brap_radio/cubits/preferences_cubit.dart';
import 'package:brap_radio/cubits/user_cubit.dart';
import 'package:brap_radio/cubits/vote_cubit.dart';
import 'package:brap_radio/cubits/vote_progress_cubit.dart';
import 'package:brap_radio/models/database/event_database_api.dart';
import 'package:brap_radio/models/database/history_database_api.dart';
import 'package:brap_radio/models/request/event_entity.dart';
import 'package:brap_radio/models/request/music_entity.dart';
import 'package:brap_radio/models/request/request_types.dart';
import 'package:brap_radio/models/user_preferences.dart';
import 'package:brap_radio/views/login.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:loader_overlay/loader_overlay.dart';
import 'package:pointycastle/api.dart';
import 'package:pointycastle/asymmetric/api.dart';

import 'request/request.dart';

List<T> _getListFromJson<T>(String json){
  return (jsonDecode(json) as List).map((v) => v as T).toList();
}

class ServiceApi {
  static const MethodChannel _channel = const MethodChannel('brap.radio/service_api');
  static const HISTORY_BUFFER_SIZE = 5; // todo change to user preference ?

  static final chatCubit = ChatCubit();
  static final historyCubit = HistoryCubit();
  static final eventCubit = EventsCubit();
  static final playingCubit = PlayingCubit();
  static final playingProgressCubit = PlayingProgressCubit();
  static final userCubit = UserCubit();
  static final voteCubit = VoteCubit();
  static final connectionCubit = ConnectionCubit();
  static final voteProgressCubit = VoteProgressCubit();
  static final muteCubit = MuteCubit();
  static final preferencesCubit = PreferencesCubit();

  static RSAPublicKey _publicKey;

  static List<MusicEntity> _historyListBuffer;

  static bool ignoreVoteActions = false;

  static void init(){
    _channel.setMethodCallHandler(callHandler);

    connectionCubit.stream.listen((event) {
      if(event is Connecting)
        _channel.invokeMethod("connect");
      else if (event is Disconnected)
        _channel.invokeMethod("disconnect");
      else if (event is LoggingIn){
        if(_publicKey == null)
          sendServerRequest(Request(RequestTypes.GET_KEY, false, null));
        else
          _loginRequest(event.id, event.password);
      }
    });

    preferencesCubit.stream.listen((event) {
      if(event is UpdatedPreference){

        if(event.preferenceName == UserPreferencesName.GET_HISTORY &&
            (event.newPreferencesValue as bool)){

          getHistory();
        }
      }
    });

    _channel.invokeMethod("initService");

  }


  static sendServerRequest(Request req){
    print("[ServiceApi] sendServerRequest called with request ${req.toString()}");
    _sendServerRequest(jsonEncode(req.toJson()));
  }
  
  static _sendServerRequest(String jsonReq){
    print("[ServiceApi] sendServerRequest called with JSON $jsonReq");
    _channel.invokeMethod("sendServerRequest", jsonReq);
  }


  static Future<dynamic> callHandler(MethodCall call) async {
    switch(call.method) {

      case "onConnectionError":
        connectionCubit.disconnected();
        await Get.dialog(ErrorDialog(
            title: "Erreur",
            description: "La connection a été rompue ; veuillez vous reconnectez a la radio (details : ${call.arguments ?? "aucuns"})")
        );
        Get.offAll(() =>
            LoaderOverlay(child: LoginPage())
        );
        break;

      case "stoppedMusicThreads":
        if(playingCubit.state is PlayingNow) { // should always be true at this point
          historyCubit.addOrUpdateLast(
              MusicDatabaseModel
                  .fromMusicEntity((playingCubit.state as PlayingNow).music)
                  .copyWithUpdatedRatio(voteProgressCubit.state)
          );
        }

        playingCubit.setDisconnected();
        playingProgressCubit.reset();
        voteCubit.resetVote();
        voteProgressCubit.reset();
        break;

      case "onErrorOnMusicReceiverInit":
        print("Error, couldn't init start player / receiver threads");
        // todo alert user
        break;

      case "refreshMusicProgress":
        final double progress = call.arguments;
        playingProgressCubit.refresh(progress);
        break;

      case "resetMusicProgress":
        playingProgressCubit.reset();
        break;

      case "refreshMuteStatus":
        final bool isMute = call.arguments;
        muteCubit.refreshMute(isMute);
        break;

      case "refreshVoteStatus":
        final int vote = call.arguments;
        if(vote == 0)
          voteCubit.resetVote();
        else
          voteCubit.refreshVote(vote == 1);
        break;

      case "onReceivedRequest":
        requestHandler(Request.fromJson(jsonDecode(call.arguments as String)));
        break;

      case 'updateConnectionState':
        final List<String> args = _getListFromJson<String>(call.arguments);

        switch(args[0]){
          case "DISCONNECTED":
            connectionCubit.disconnected();
            break;
          case "CONNECTED":
            connectionCubit.connected();
            break;
          case "CONNECTION_ERROR":
            connectionCubit.connectionError(args.length > 1 ? args[1]
                : ConnectionErrorNames.UNKNOWN);
            break;
        }
        break;

      default:
        print("Received unknown method call : ${call.method}");
        break;
    }
  }

  static void requestHandler(Request req){
    switch(req.type){

      case RequestTypes.CHAT_MSG:
        final args = req.stringArgs;
        if(args != null && args.length >= 3){
          chatCubit.add(
              Message(
                  args[0] == userCubit.nickname ? "SELF" : args[0],
                  args[1], args[2])
          );
        }
        break;

      case RequestTypes.GET_KEY:
        if(req.stringArgs != null && req.stringArgs.length >= 2)
        _publicKey = RSAPublicKey(
            BigInt.parse(req.stringArgs[0]),
            BigInt.parse(req.stringArgs[1]),
        );

        // if trying to log in, resend login request encoded with new key
        final state = connectionCubit.state;
        if(state is LoggingIn)
          _loginRequest(state.id, state.password);

        break;

      case RequestTypes.REFRESH_VOTES:

        if(req.stringArgs != null && req.stringArgs.length > 0) { // contains reset flag

          /* in this particular case, music ended from the server side, but due
             to latency, it is still playing client side. To avoid any confusion,
             any vote action (refresh or voting change) should be ignored
             to match last state before reset (== music change)

             a GET_VOTES_INFOS should be send, and ignoreVoteActions turned false
             on next "LOCAL_PLAYING" request

             todo inform user ?
          */
          ignoreVoteActions = true;

          // adding to history
          if(playingCubit.state is PlayingNow){ // should always be true here
            historyCubit.addOrUpdateLast(
                MusicDatabaseModel
                    .fromMusicEntity((playingCubit.state as PlayingNow).music)
                    .copyWithUpdatedRatio(voteProgressCubit.state)
            );
          }

        } else {
          if(req.intArgs != null && req.intArgs.length >= 2){
            final numberOfVotes = req.intArgs[0];
            final numberOfPositiveVotes = req.intArgs[1];

            if(numberOfVotes == 0) // avoid division by 0
              voteProgressCubit.emit(0.5);
            else
              voteProgressCubit.refresh(numberOfPositiveVotes / numberOfVotes);
          }
        }

        break;

      case RequestTypes.GET_VOTES_INFOS:
        if(req.intArgs != null && req.intArgs.length >= 2){
          final numberOfVotes = req.intArgs[0];
          final numberOfPositiveVotes = req.intArgs[1];

          if(numberOfVotes == 0) // avoid division by 0
            voteProgressCubit.emit(0.5);
          else
            voteProgressCubit.refresh(numberOfPositiveVotes / numberOfVotes);
        }
        break;

      case RequestTypes.LOCAL_PLAYING:
        if(req.musicArgs != null && req.musicArgs.length > 0){
          final m = req.musicArgs[0];
          int totalVotes = m.totalVoteNumber;

          if(totalVotes == 0) // avoid division by 0
            voteProgressCubit.emit(0.5);
          else
            voteProgressCubit.emit(m.positiveVoteNumber / totalVotes);

          playingCubit.nowPlaying(m);
          historyCubit.addOrUpdateLast(
              MusicDatabaseModel.fromMusicEntity(m).copyWithUpdatedRatio(-1)
          );

          // see refresh vote case for explanations about instructions below
          ignoreVoteActions = false;
          sendServerRequest(Request(RequestTypes.GET_VOTES_INFOS, false, null));
          // ***

        }
        break;

      case RequestTypes.GET_HISTORY:

        /*
         * retrieve whole history from server, then setting it to local history
         */

        final musics = req.musicArgs;

        if(musics != null){
          _historyListBuffer ??= [];
          _historyListBuffer.addAll(musics);

          if(musics.length >= HISTORY_BUFFER_SIZE){
            sendServerRequest(Request(RequestTypes.GET_HISTORY, false, null)
              ..addIntArgs([_historyListBuffer.length,
                _historyListBuffer.length + HISTORY_BUFFER_SIZE]
              )
            );
          } else {
            for(MusicEntity m in _historyListBuffer.reversed){
              historyCubit.addOrUpdateLast(MusicDatabaseModel.fromMusicEntity(m));
            }

            _historyListBuffer = null;
          }
        }

        break;
      
      case RequestTypes.GET_EVENT:
        _setEvent(req.eventArgs);
        break;

      case RequestTypes.ADD_EVENT:

        if(req.eventArgs != null){
          for(EventEntity e in req.eventArgs){
            eventCubit.add(EventDatabaseModel.fromEventEntity(e));
          }
        }
        break;
      
      case RequestTypes.DELETE_EVENT:

        if(req.eventArgs != null) {
          for(EventEntity e in req.eventArgs){
            eventCubit.delete(EventDatabaseModel.fromEventEntity(e));
          }
        }
        break;

      case RequestTypes.LOGIN:
        if(req.isError){

          if(req.errorMessage == "BAD ENCODE"){

            sendServerRequest(Request(RequestTypes.GET_KEY, false, null));

          } else {
            connectionCubit.loginError(req.errorMessage);
          }

        } else {
          final user = req.userArgs[0]; // blind trust in server ans, idk if best thing
          connectionCubit.loggedIn();
          userCubit.loggedIn(user.nickname, user.email, user.status);
          _setEvent(req.eventArgs);
          if(UserPreferences().getHistory){
            sendServerRequest(Request(RequestTypes.GET_HISTORY, false, null)
              ..addIntArgs([0, HISTORY_BUFFER_SIZE]));
          }
        }
        break;

      default:
        print("unknown / unhandled request received : ${req.type}");
        break;
    }
  }

  static bool _loginRequest(String id, String pwd){
    if(_publicKey == null) return false;

    final req = Request(RequestTypes.LOGIN, false, null)..addStringArgs([
      encrypt("ID:$id"),
      encrypt("PWD:$pwd")
      // if the string argument is equals to the preference,
      // then it is already encrypted
    ]);

    sendServerRequest(req);
    return true;
  }

  static void getHistory(){
    historyCubit.reset();
    sendServerRequest (
        Request(RequestTypes.GET_HISTORY, false, null)
          ..addIntArgs([0, HISTORY_BUFFER_SIZE])
    );
  }

  static void startPlaying(){
    _channel.invokeMethod("startMusicThreads");
  }  // from secs to ms

  static void disconnectPlaying() => _channel.invokeMethod("stopMusicThreads");

  static void mute(bool isMute) => _channel.invokeMethod("mute", isMute);

  static void sendMessage(String msg) => sendServerRequest(
      Request(RequestTypes.CHAT_MSG, false, null)..addStringArgs([msg])
  );

  static void vote(bool isPositive) =>
      _channel.invokeMethod("vote", isPositive ? 1 : -1);

  static void _setEvent(List<EventEntity> events){
    /* getting complete list from server, so deleting everything existing,
     * and adding the whole list to the db
     *
     * todo optimize process
     * (could just check if db row in event list, then do nothing, otherwise delete it,
     * then make sure every item of event list in db (add if not))
     */

    eventCubit.reset();
    if(events != null){
      for(var e in events)
        eventCubit.add(EventDatabaseModel.fromEventEntity(e));
    }
  }

  static String encrypt(String text){
    final p = AsymmetricBlockCipher('RSA/OAEP');
    p.init(true, PublicKeyParameter<RSAPublicKey>(_publicKey));

    final cipher = p.process(Utf8Encoder().convert(text));

    return Base64Encoder().convert(cipher);
  }

  /*static void requestHandler(Request req){
    switch(req.type){

      case RequestTypes.CHAT_MSG:
        if(req.stringArgs != null && req.userArgs != null &&
            req.stringArgs.length == req.userArgs.length){

          req.userArgs.asMap().forEach((i, author) {
            String status = "OTHERS";
            if(author.status != null && author.status.length > 0) // todo find greater role
              status = author.status[0];

            // todo add to bloc here
          });
          break;

        }
    }
  }*/
}

