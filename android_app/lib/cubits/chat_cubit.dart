import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';

part 'chat_state.dart';


class ChatCubit extends Cubit<ChatState> {
  ChatCubit() : super(ChatInitial()) {
    emit(ChatRefreshed(messages, null));
  }

  final List<Message> messages = [];

  void add(Message newMessage){
    messages.add(newMessage);

    if(messages.length > 50) // todo msg list length in user prefs ?
      messages.removeLast();

    emit(ChatRefreshed(messages, newMessage));
  }
}
