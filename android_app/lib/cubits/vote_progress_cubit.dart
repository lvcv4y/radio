import 'package:bloc/bloc.dart';

class VoteProgressCubit extends Cubit<double> {
  VoteProgressCubit() : super(0.5);

  void refresh(double newValue) => emit(newValue);

  void reset() => emit(0.5);
}
