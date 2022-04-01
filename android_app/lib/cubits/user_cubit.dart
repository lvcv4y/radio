import 'package:bloc/bloc.dart';
import 'package:brap_radio/models/user_preferences.dart';
import 'package:equatable/equatable.dart';
import 'package:shared_preferences/shared_preferences.dart';

part 'user_state.dart';

class UserCubit extends Cubit<UserState> {

  static const DEFAULT_PSEUDO = 'NICKNAME';
  static const DEFAULT_EMAIL = "EMAIL";
  static const DEFAULT_PWD = "PASSWORD_HASH";



  SharedPreferences _preferences;
  String nickname, email, pwd;
  List<String> status = [];

  UserCubit() : super(UserInitial()) {
    init();
  }

  init() async {
    _preferences = await SharedPreferences.getInstance();
    nickname = _preferences.getString(UserPreferencesName.NICKNAME) ?? DEFAULT_PSEUDO;
    email = _preferences.getString(UserPreferencesName.EMAIL) ?? DEFAULT_EMAIL;
    pwd = _preferences.getString(UserPreferencesName.PASSWORD) ?? DEFAULT_PWD;
    _emitRefreshedUserModel();
  }

  void refreshEmail(String newEmail){
    if(email != newEmail){
      email = newEmail;
      if(UserPreferences().rememberMe)
        _preferences.setString(UserPreferencesName.EMAIL, newEmail);
      _emitRefreshedUserModel();
    }
  }

  void refreshNickname(String newNickname){
    if(nickname != newNickname){
      nickname = newNickname;
      if(UserPreferences().rememberMe)
        _preferences.setString(UserPreferencesName.NICKNAME, newNickname);
      _emitRefreshedUserModel();
    }
  }

  void refreshStatus(List<String> newStatus){
    if(status != newStatus){
      status = newStatus;
      _emitRefreshedUserModel();
    }
  }

  void loggedIn(String nickname, String email, List<String> status){
    this.nickname = nickname;
    this.email = email;
    this.status = status;

    if(UserPreferences().rememberMe){
      _preferences.setString(UserPreferencesName.NICKNAME, nickname);
      _preferences.setString(UserPreferencesName.EMAIL, nickname);
    }
      _emitRefreshedUserModel();
  }

  void _emitRefreshedUserModel() => emit(UserModel(nickname, email, pwd, status));
}
