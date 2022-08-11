package com.schedch.mvp.service;

import com.schedch.mvp.model.Participant;
import com.schedch.mvp.model.Room;
import com.schedch.mvp.model.Schedule;
import com.schedch.mvp.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class DayParticipantService {

    private final RoomService roomService;
    private final ParticipantRepository participantRepository;

    public Participant findParticipant(
            String roomUuid, String participantName, String password) throws IllegalAccessException {
        Room room = roomService.getRoom(roomUuid);
        List<Participant> foundParticipant = room.findUnSignedParticipant(participantName);

        if(foundParticipant.isEmpty()) {
            //신규 유저 -> 유저 등록해야 함
            Participant newParticipant = new Participant(participantName, password, false);
            room.addParticipant(newParticipant);
            return newParticipant;
        }

        Participant participant = foundParticipant.get(0);

        if(participant.checkPassword(password)) { //기존 유저가 맞음 -> 기존 시간 돌려주면 됨
            return participant;
        }
        else { //기존 유저이나, 비밀번호가 틀렸음
            throw new IllegalAccessException("password is incorrect for participant: " + participantName);
        }
    }

    public void saveParticipantAvailable(String roomUuid, String participantName, List<LocalDate> localDateList) {
        Room room = roomService.getRoom(roomUuid);

        Participant participant = participantRepository.findParticipantByParticipantNameAndRoom(participantName, room)
                .orElseThrow(() -> new NoSuchElementException(String.format("Participant not found for name: %s", participantName)));

        participant.emptySchedules();
        localDateList.stream().forEach(localDate -> {
            participant.addSchedule(new Schedule(localDate));
        });
    }


}
