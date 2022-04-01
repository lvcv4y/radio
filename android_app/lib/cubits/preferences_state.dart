part of 'preferences_cubit.dart';

abstract class PreferencesState extends Equatable {
  const PreferencesState();
}

class PreferencesInitial extends PreferencesState {
  @override
  List<Object> get props => [];
}

class UpdatedPreference extends PreferencesState {
  final String preferenceName;
  final dynamic newPreferencesValue;

  const UpdatedPreference(this.preferenceName, this.newPreferencesValue);

  @override
  List<Object> get props => [preferenceName, newPreferencesValue];
}
