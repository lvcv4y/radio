import 'package:bloc/bloc.dart';
import 'package:brap_radio/models/database/history_database_api.dart';
import 'package:equatable/equatable.dart';

part 'history_state.dart';

class HistoryCubit extends Cubit<HistoryState> {
  HistoryCubit() : super(HistoryInitial());

  List<MusicDatabaseModel> history;
  final HistoryDatabaseAPI databaseAPI = HistoryDatabaseAPI();

  void initHistory() async {
    history = await databaseAPI.getHistory() ?? [];
    if(history.length > 0)
      history = List.from(history.reversed);

    emit(HistoryRefreshed(history, null));
  }

  void addOrUpdateLast(MusicDatabaseModel musicToAdd){

    final toAddIndex = history.indexWhere(
            (element) => (element.authors == musicToAdd.authors
                && element.title == musicToAdd.title)
    );

    if(toAddIndex == -1){ // if no music in history has same authors / title as musicToAdd

      databaseAPI.add(musicToAdd);
      history.insert(0, musicToAdd);

      while(history.length > 50){
        history.removeLast();
      }

      emit(HistoryRefreshed(history, musicToAdd));
    } else if(toAddIndex == 0){ // trying to add to history the last music, in other words, updating it
      // the only thing that can be updated is the vote ratio (other things are
      // linked to the music itself, and proposedBy shouldn't change)

      if(history[0].positiveVoteRatio != musicToAdd.positiveVoteRatio) {
        databaseAPI.updateLastElementVoteRatio(musicToAdd.positiveVoteRatio);
        history[0] = musicToAdd;
        emit(HistoryUpdatedRatio(history, musicToAdd.positiveVoteRatio));
      }
    }
  }

  void reset(){
    databaseAPI.resetDatabase();
  }
}
