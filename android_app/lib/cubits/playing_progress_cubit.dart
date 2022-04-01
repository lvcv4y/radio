import 'package:flutter_bloc/flutter_bloc.dart';

class PlayingProgressCubit extends Cubit<double> {
  PlayingProgressCubit() : super(null);

  void refresh(double value) => emit(value);

  void reset(){
    emit(null);
  }
}