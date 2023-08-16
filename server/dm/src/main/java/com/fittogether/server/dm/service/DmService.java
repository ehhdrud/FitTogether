package com.fittogether.server.dm.service;

import com.fittogether.server.dm.domain.dto.MessageForm;
import com.fittogether.server.dm.domain.entity.ChatRoom;
import com.fittogether.server.dm.domain.entity.Message;
import com.fittogether.server.dm.domain.repository.ChatRoomRepository;
import com.fittogether.server.dm.domain.repository.MessageRepository;
import com.fittogether.server.domain.token.JwtProvider;
import com.fittogether.server.domain.token.UserVo;
import com.fittogether.server.user.domain.model.User;
import com.fittogether.server.user.domain.repository.UserRepository;
import com.fittogether.server.user.exception.UserCustomException;
import com.fittogether.server.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DmService {
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    //방 생성
    @Transactional
    public ChatRoom createDmRoom(String token, String receiverNickname) {
        if (!jwtProvider.validateToken(token)) {
            throw new UserCustomException(UserErrorCode.NOT_FOUND_USER);
        }

        UserVo userVo = jwtProvider.getUserVo(token);

        User sender = userRepository.findByNickname(userVo.getNickname())
                .orElseThrow(() -> new UserCustomException(UserErrorCode.NOT_FOUND_USER));

        User receiver = userRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new UserCustomException(UserErrorCode.NOT_FOUND_USER));

        ChatRoom chatRoom = ChatRoom.builder()
                .senderId(sender)
                .receiverId(receiver)
                .senderNickname(sender.getNickname())
                .receiverNickname(receiver.getNickname())
                .chatRoomDt(LocalDateTime.now())
                .build();
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    // 메세지 보내기
    public Message sendMessage(
            //  String token,
            MessageForm messageForm
    ) {

      /*  if (!jwtProvider.validateToken(token)) {
            throw new UserCustomException(UserErrorCode.NOT_FOUND_USER);
        }

         UserVo userVo = jwtProvider.getUserVo(token);

       */


        //추후 토큰 사용자로 변경해야함
        ChatRoom chatRoom = chatRoomRepository.findById(messageForm.getChatRoomId())
                .orElseThrow(() -> new UserCustomException(UserErrorCode.NOT_FOUND_USER));

        User sender = userRepository.findByNickname(messageForm.getSenderNickname())
                .orElseThrow(() -> new UserCustomException(UserErrorCode.NOT_FOUND_USER));


        // 데이터베이스에서 마지막 id 값을 가져옴 id값이 null일경우 +1
        Long currentMaxMessageId = messageRepository.findMaxMessageId();
        currentMaxMessageId = (currentMaxMessageId != null) ? currentMaxMessageId + 1 : 1;

        Message message = Message.builder()
                .messageId(currentMaxMessageId)
                .chatRoomId(chatRoom)
                .senderId(sender)
                .senderNickname(sender.getNickname())
                .contents(messageForm.getContents())
                .sendDt(LocalDateTime.now())
                .build();
        messageRepository.save(message);
        return message;
    }

    // 채팅방 목록 조회
    public List<ChatRoom> dmLists(String token) {
        if (!jwtProvider.validateToken(token)) {
            throw new UserCustomException(UserErrorCode.NOT_FOUND_USER);
        }

        UserVo userVo = jwtProvider.getUserVo(token);

        User user = userRepository.findByNickname(userVo.getNickname())
                .orElseThrow(() -> new UserCustomException(UserErrorCode.NOT_FOUND_USER));

        // 로그인한 사용자가 먼저 메세지를 보낸 경우
        List<ChatRoom> sentMessage = chatRoomRepository.findAllBySenderNickname(
                user.getNickname());

        // 로그인한 사용자가 메세지를 받은 경우
        List<ChatRoom> receivedMessage = chatRoomRepository.findAllByReceiverNickname(
                user.getNickname());

        List<ChatRoom> dmList = new ArrayList<>();
        dmList.addAll(sentMessage);
        dmList.addAll(receivedMessage);


        return dmList;
    }


    //해당 채팅방 메세지 조회
    public List<Message> messageLists(String token,Long chatRoomId){
        if (!jwtProvider.validateToken(token)) {
            throw new UserCustomException(UserErrorCode.NOT_FOUND_USER);
        }


        //message 의 chatRoomId는 ChatRoom의 Id값을 참조하기때문에 따로 값을 추출
        Optional<ChatRoom> room=chatRoomRepository.findById(chatRoomId);

        List<Message> roomId=messageRepository.findAllByChatRoomId(room);

        return roomId;

    }


}
