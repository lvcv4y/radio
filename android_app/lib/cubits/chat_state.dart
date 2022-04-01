part of 'chat_cubit.dart';

class Message {
  final String userFrom;
  final String messageContent;
  final String authorStatus;

  Message(this.userFrom, this.messageContent, this.authorStatus);
}

abstract class ChatState extends Equatable {
  const ChatState();
}

class ChatInitial extends ChatState {
  @override
  List<Object> get props => [];
}

class ChatRefreshed extends ChatState {
  final List<Message> messages;
  final Message added;

  const ChatRefreshed(this.messages, this.added);

  @override
  List<Object> get props => [messages, added];
}

