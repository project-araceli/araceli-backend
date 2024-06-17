package at.araceli.backend.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project: araceli-backend
 * Created by: Michael Hütter
 * Created at: 16.06.2024
 */

@RestController
@RequestMapping("/demo")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class DemoController {

    @GetMapping
    public String demo(HttpServletRequest request) {
        return "Hello " + request.getUserPrincipal().getName();
    }
}
