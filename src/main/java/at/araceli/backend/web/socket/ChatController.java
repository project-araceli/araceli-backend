package at.araceli.backend.web.socket;

import at.araceli.backend.db.UserRepository;
import at.araceli.backend.pojos.IncomingMessage;
import at.araceli.backend.pojos.Message;
import at.araceli.backend.pojos.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 15.06.24
 */

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final UserRepository userRepo;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message sendChat(IncomingMessage message) {
        User user = userRepo.findByToken(message.getToken()).orElse(null);
        return new Message(UUID.randomUUID().toString(), user, message.getContent(), LocalDateTime.now());
    }
}
