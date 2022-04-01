import 'package:bloc/bloc.dart';
import 'package:brap_radio/models/service_api.dart';
import 'package:equatable/equatable.dart';

part 'vote_state.dart';

class VoteCubit extends Cubit<VoteState> {
  VoteCubit() : super(VoteState(VoteStatus.NO_VOTE));

  void resetVote() => emit(VoteState(VoteStatus.NO_VOTE));


  // refresh cubit **AND** callback to background service,
  // while refreshVote() ONLY REFRESH THE CUBIT
  // Hence, vote() should be used by GUI widgets, while refreshVote() should
  // ONLY be used by ServiceAPI to, well, refresh those widgets

  void vote(bool isPositive) {
    if(!ServiceApi.ignoreVoteActions){
      ServiceApi.vote(isPositive);
      refreshVote(isPositive);
    }
  }

  void refreshVote(bool isPositive) => emit(
      VoteState(isPositive ? VoteStatus.POSITIVE_VOTE : VoteStatus.NEGATIVE_VOTE)
  );
}
