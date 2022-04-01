import 'package:equatable/equatable.dart';

import 'request.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_serializers/event_entity.g.dart';

@JsonSerializable()
class EventEntity extends Equatable {
  final String title, description;
  final DateTime startAt, endAt;

  const EventEntity(this.title, this.description, this.startAt, this.endAt);

  factory EventEntity.fromJson(Map<String, dynamic> data) => _$EventEntityFromJson(data);

  Map<String, dynamic> toJson() => _$EventEntityToJson(this);
  Map<String, dynamic> toMap() => toJson();

  List<Object> get props => [title, description, startAt, endAt];

}