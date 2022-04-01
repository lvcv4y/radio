import 'package:bloc/bloc.dart';

part 'connection_state.dart';

class ConnectionCubit extends Cubit<SocketConnectionState> {
  ConnectionCubit() : super(Disconnected());

  void connect() => emit(Connecting());

  void connected() => emit(Connected());

  void connectionError(String error) => emit(ConnectionError(error));

  void login(String id, String passwordHash) => emit(LoggingIn(id, passwordHash));

  void loggedIn() => emit(LoggedIn());

  void loginError(String error) => emit(LoginError(error));

  void disconnected() => emit(Disconnected());

  void disconnect() => emit(Disconnect());

}
