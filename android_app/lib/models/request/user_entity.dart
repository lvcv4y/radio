
import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_serializers/user_entity.g.dart';

@JsonSerializable()
class UserEntity extends Equatable {
  final int id, credits;

  final String email, nickname;

  final List<String> status;

  UserEntity(this.id, this.email, this.nickname, this.status, this.credits);

  factory UserEntity.fromJson(Map<String, dynamic> data) => _$UserEntityFromJson(data);

  Map<String, dynamic> toJson() => _$UserEntityToJson(this);

  List<Object> get props => [email, nickname, status, credits];
}

class Status {
  static const ADMIN = "ADMIN";
  static const MOD = "MODO";
  static const MEMBER = "MEMBER";

  static const OTHER = "OTHER";
  static const SELF = "SELF";
}