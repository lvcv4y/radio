import 'package:bloc/bloc.dart';
import 'package:brap_radio/models/request/music_entity.dart';
import 'package:brap_radio/models/service_api.dart';
import 'package:equatable/equatable.dart';

part 'playing_state.dart';

class PlayingCubit extends Cubit<PlayingState> {
  PlayingCubit() : super(PlayingInitial());

  void nowPlaying(MusicEntity music){
    emit(PlayingNow(music));
  }

  // same difference as vote() and refreshVote() in vote_cubit.dart (VoteCubit class)

  void disconnect(){
    ServiceApi.disconnectPlaying();
  }

  void setDisconnected(){
    emit(PlayingDisconnect());
  }

  void connect(){
    ServiceApi.startPlaying();
    emit(PlayingConnecting());
  }

  void onConnectionError(String errorMessage){
    emit(PlayingError(errorMessage));
  }
}
