package at.araceli.backend.web.socket;

import at.araceli.backend.db.UserRepository;
import at.araceli.backend.pojos.IncomingMessage;
import at.araceli.backend.pojos.Message;
import at.araceli.backend.pojos.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ChatController {

    private final UserRepository userRepo;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message sendChat(IncomingMessage message) {
        User anonymous = new User();
        anonymous.setUsername("anonymous");
        User user = userRepo.findByUsername(message.getUsername()).orElse(anonymous);
        return new Message(UUID.randomUUID().toString(), user, message.getContent(), LocalDateTime.now());
    }
}
