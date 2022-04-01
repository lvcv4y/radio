part of 'playing_cubit.dart';

abstract class PlayingState extends Equatable {
  const PlayingState();
}

class PlayingInitial extends PlayingState {
  @override
  List<Object> get props => [];
}

class PlayingNow extends PlayingState {
  final MusicEntity music;

  const PlayingNow(this.music);

  @override
  List<Object> get props => [music];
}

class PlayingDisconnect extends PlayingState {
  @override
  List<Object> get props => [];
}

class PlayingConnecting extends PlayingState {
  @override
  List<Object> get props => [];
}

class PlayingError extends PlayingState {
  final String errorMessage;

  const PlayingError(this.errorMessage);

  @override
  List<Object> get props => [errorMessage];
}