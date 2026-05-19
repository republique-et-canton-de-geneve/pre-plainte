package ch.ge.police.core.domain.model.rendezvous;

public record CreneauRendezVous(
  String id,
  String date,
  String heureDebut,
  String heureFin,
  String lieu,
  String codeRdv
) {}
