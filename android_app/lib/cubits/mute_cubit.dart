import 'package:bloc/bloc.dart';
import 'package:brap_radio/models/service_api.dart';

class MuteCubit extends Cubit<bool> {
  MuteCubit() : super(false);

  void mute(bool isMute){
    ServiceApi.mute(isMute);
    emit(isMute);
  }

  void refreshMute(bool isMute){
    emit(isMute);
  }
}
