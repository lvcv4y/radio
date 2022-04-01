import 'package:equatable/equatable.dart';

import 'request.dart';
import 'file:///C:/Users/psyfi/AndroidStudioProjects/brap_radio/lib/models/request/user_entity.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_serializers/sanction_entity.g.dart';

@JsonSerializable(explicitToJson: true)
class SanctionEntity extends Equatable {
  final int id;
  final String type, description;
  final UserEntity user;
  final DateTime startAt, endAt;

  SanctionEntity(this.id, this.type, this.description, this.user, this.startAt, this.endAt);

  factory SanctionEntity.fromJson(Map<String, dynamic> data) => _$SanctionEntityFromJson(data);

  Map<String, dynamic> toJson() => _$SanctionEntityToJson(this);

  List<Object> get props => [type, description, user, startAt, endAt];
}