class RequestTypes {
  static const GET_HISTORY = "GET_HISTORY";
  static const ADD_EVENT = "ADD_EVENT";
  static const DELETE_EVENT = "DELETE_EVENT";
  static const GET_EVENT = "GET_EVENT";
  static const CHAT_MSG = "CHAT_MSG";
  static const LOGIN = "LOGIN";

  static const GET_VOTES_INFOS = "GET_VOTES_INFOS";
  static const REFRESH_VOTES = "REFRESH_VOTES";

  static const GET_KEY = "GET_KEY";

  // called in local, actually never "received", shouldn't be sent
  static const LOCAL_PLAYING = "LOCAL_PLAYING";

  // flags
  static const RESET_VOTES_FLAG = "RESET";
}