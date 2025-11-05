package com.kudos.dto;

public record KudosDTO(String message, Long recipientEmployeeId, Long recipientTeamId, boolean isComment,
        boolean anonymous) {
}
