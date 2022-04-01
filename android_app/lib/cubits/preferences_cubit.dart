import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:shared_preferences/shared_preferences.dart';

part 'preferences_state.dart';

class PreferencesCubit extends Cubit<PreferencesState> {
  PreferencesCubit() : super(PreferencesInitial()){
    init();
  }

  SharedPreferences _preferences;
  Map<Type, Function> typeToSetterMap;
  void init() async {
    _preferences = await SharedPreferences.getInstance();
    typeToSetterMap = {
      String: _preferences.setString,
      bool: _preferences.setBool,
      double: _preferences.setDouble,
      int: _preferences.setInt
    };
  }

  void updatePreference(String preferenceName, dynamic newValue){
    // print("$preferenceName updated to $newValue");
    typeToSetterMap[newValue.runtimeType](preferenceName, newValue);
    emit(UpdatedPreference(preferenceName, newValue));
  }
}
