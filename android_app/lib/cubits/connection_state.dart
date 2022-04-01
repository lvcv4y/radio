part of 'connection_cubit.dart';

// no use of equatable, because otherwise, if an error is thrown twice, the second
// one will be ignored (because of equality test returning true (== the whole point of using Equatable))
abstract class SocketConnectionState {
  const SocketConnectionState();
}

// connection states (before logging in)

class Disconnect extends SocketConnectionState {}

class Disconnected extends SocketConnectionState {}

class Connecting extends SocketConnectionState {}

class Connected extends SocketConnectionState {}

class ConnectionError extends SocketConnectionState {
  const ConnectionError(this.error);
  final String error;
}

class ConnectionErrorNames {
  static const NO_INTERNET_FOUND = "NO_INTERNET_FOUND";
  static const UNKNOWN = "UNKNOWN";
}

// login Status

class LoggingIn extends SocketConnectionState {

  const LoggingIn(this.id, this.password);

  final String id, password;
}

class LoggedIn extends SocketConnectionState {}

class LoginError extends SocketConnectionState {
  const LoginError(this.error);
  final String error;
}

class LoginErrorNames {
  static const BAD_PASSWORD = "BAD PWD";
  static const BAD_ID = "NOT FOUND";
  static const SERVER_ERROR = "SERVER ERROR";
  static const MISSING_ARGS = "MISSING ARGS";
  static const BANNED = "BANNED";
  static const UNKNOWN = "UNKNOWN";
}