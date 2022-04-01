part of 'user_cubit.dart';

abstract class UserState extends Equatable {
  const UserState();
}

class UserInitial extends UserState {
  @override
  List<Object> get props => [];
}

class UserModel extends UserState {

  const UserModel(this.nickname, this.email, this.passwordHash, this.status);

  final String nickname;
  final String email;
  final String passwordHash;
  final List<String> status;

  @override
  List<Object> get props => [nickname, email, passwordHash, status];
}