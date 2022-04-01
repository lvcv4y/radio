part of 'history_cubit.dart';

abstract class HistoryState extends Equatable {
  final List<MusicDatabaseModel> history;
  const HistoryState(this.history);
}

class HistoryInitial extends HistoryState {
  HistoryInitial() : super([]);
  @override
  List<Object> get props => [];
}

class HistoryRefreshed extends HistoryState {

  final MusicDatabaseModel addedMusic;

  HistoryRefreshed(List<MusicDatabaseModel> history, this.addedMusic) : super(history);

  @override
  List<Object> get props => [history, addedMusic];
}

class HistoryUpdatedRatio extends HistoryState {
  final double newRatio;

  HistoryUpdatedRatio(List<MusicDatabaseModel> history, this.newRatio) : super(history);

  @override
  List<Object> get props => [history, newRatio];

}