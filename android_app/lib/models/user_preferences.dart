import 'package:shared_preferences/shared_preferences.dart';

class UserPreferences {
  static final UserPreferences _instance = UserPreferences._ctor();
  SharedPreferences _preferences;
  Map<Type, Function> _typeToGetterMap;

  factory UserPreferences(){
    return _instance;
  }

  UserPreferences._ctor();

  Future init() async {
    _preferences = await SharedPreferences.getInstance();
    _typeToGetterMap = {
      String: _preferences.getString,
      bool: _preferences.getBool,
      double: _preferences.getBool,
      int: _preferences.getInt
    };
  }

  get anonymousMode => _preferences.getBool(UserPreferencesName.ANONYMOUS_MODE) ?? true;

  get showNickname => _preferences.getBool(UserPreferencesName.SHOW_NICKNAME) ?? true;

  get showEmail => _preferences.getBool(UserPreferencesName.SHOW_EMAIL) ?? false;

  get showPropositions =>  _preferences.getBool(UserPreferencesName.SHOW_PROPOSITIONS) ?? true;

  get showStatus => _preferences.getBool(UserPreferencesName.SHOW_STATUS) ?? true;

  get nightMode => _preferences.getBool(UserPreferencesName.NIGHT_MODE) ?? false;

  get ecoMode => _preferences.getBool(UserPreferencesName.ECO_MODE) ?? false;

  get getHistory => _preferences.getBool(UserPreferencesName.GET_HISTORY) ?? false;

  get theme => _preferences.getString(UserPreferencesName.THEME) ?? 'default';

  get privacyMode => _preferences.getString(UserPreferencesName.PRIVACY_MODE) ?? "public";

  get voteRefreshingDelay => _preferences.getDouble(UserPreferencesName.VOTE_REFRESH_DELAY) ?? 0.5;

  get rememberMe => _preferences.getBool(UserPreferencesName.REMEMBER_ME) ?? false;

  get nickname => _preferences.getString(UserPreferencesName.NICKNAME) ?? 'NICKNAME';

  get email => _preferences.getString(UserPreferencesName.EMAIL) ?? 'EMAIL';

  dynamic getPreference(Type type, String name) => _typeToGetterMap[type](name);

}

class UserPreferencesName {
  static const ANONYMOUS_MODE = "ANONYMOUS_MODE";
  static const SHOW_NICKNAME = "SHOW_NICKNAME";
  static const SHOW_EMAIL = "SHOW_EMAIL";
  static const SHOW_PROPOSITIONS = "SHOW_PROPOSITIONS";
  static const SHOW_STATUS = "SHOW_STATUS";
  static const NIGHT_MODE = "NIGHT_MODE";
  static const ECO_MODE = "ECO_MODE";
  static const GET_HISTORY = "GET_HISTORY";
  static const THEME = "THEME";
  static const PRIVACY_MODE = "PRIVACY_MODE";
  static const VOTE_REFRESH_DELAY = "VOTE_REFRESH_DELAY";
  static const NICKNAME = "NICKNAME";
  static const EMAIL = "EMAIL";
  static const PASSWORD = "PASSWORD";
  static const REMEMBER_ME = "REMEMBER_ME";
}