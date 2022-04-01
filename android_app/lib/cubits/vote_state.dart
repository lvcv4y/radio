part of 'vote_cubit.dart';

class VoteState extends Equatable {

  const VoteState(this.status);
  final VoteStatus status;

  bool get hasVoted => status != VoteStatus.NO_VOTE;

  bool get isVotePositive => status == VoteStatus.POSITIVE_VOTE;

  bool get isVoteNegative => status == VoteStatus.NEGATIVE_VOTE;

  List<Object> get props => [status];
}

enum VoteStatus {
  NO_VOTE, POSITIVE_VOTE, NEGATIVE_VOTE
}
