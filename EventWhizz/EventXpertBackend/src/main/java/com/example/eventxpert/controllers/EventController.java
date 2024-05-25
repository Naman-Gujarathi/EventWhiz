package com.example.eventxpert.controllers;

import com.example.eventxpert.dao.Eventdao;
import com.example.eventxpert.dao.Userdao;
import com.example.eventxpert.pojo.Event;
import com.example.eventxpert.pojo.User;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.eventxpert.dao.DAO.getSession;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getCurrentEvents(Eventdao eventdao) {
        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            List<Event> events = eventdao.getAllCurrentEvents();
            jsonResponse.put("events", events);
            return ResponseEntity.status(200).body(jsonResponse);
        }
        catch(HibernateException e) {
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> getOneEvent(Eventdao eventdao, Userdao userdao, @PathVariable int eventId, @RequestAttribute int id) {
        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            Event currEvent = eventdao.getOneEvent(eventId);
            boolean isRegistered = eventdao.isRegisteredForEvent(new User(id), new Event(eventId), userdao);
            if (currEvent == null) {
                jsonResponse.put("message", "Event not found");
                return ResponseEntity.status(404).body(jsonResponse);
            }
            jsonResponse.put("event", currEvent);
            jsonResponse.put("isRegistered", isRegistered);
            return ResponseEntity.status(200).body(jsonResponse);
        }
        catch (HibernateException e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
        catch (Exception e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> updateEvent(Eventdao eventdao, Userdao userdao, @RequestBody Event event, @PathVariable String eventId, @RequestAttribute String id) {
        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            Event currEvent = eventdao.getOneEvent(Integer.parseInt(eventId));
            if (currEvent.getOrganizer().getUserId() != Integer.parseInt(id)) {
                jsonResponse.put("message", "You cannot update this event since you are not its organizer");
                return ResponseEntity.status(400).body(jsonResponse);
            }
            boolean success = eventdao.updateEvent(event, eventId);
            if (!success) {
                jsonResponse.put("message", "Something went wrong");
                return ResponseEntity.status(500).body(jsonResponse);
            }
            jsonResponse.put("event", event);
            return ResponseEntity.status(200).body(jsonResponse);
        }
        catch (Exception e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> createEvent(Eventdao eventdao, Userdao userdao, final @RequestBody Event event, @RequestAttribute String role, @RequestAttribute String id) {
        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            if (!role.equals("admin")) {
                jsonResponse.put("message", "You are not authorized to perform this action");
                return ResponseEntity.status(403).body(jsonResponse);
            }
            User organizer = userdao.getUserByUserId(Integer.parseInt(id));
            event.setOrganizer(organizer);
            eventdao.addEvent(event);
            jsonResponse.put("message", "Successfully added event");
            return ResponseEntity.status(200).body(jsonResponse);
        }
        catch(HibernateException e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
        catch(Exception e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<Map<String, Object>> registerForEvent(Eventdao eventdao, Userdao userdao, @PathVariable int eventId, @RequestAttribute String role, @RequestAttribute String id) {
        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            Event currEvent = eventdao.getOneEvent(eventId);
            if (currEvent == null) {
                jsonResponse.put("message", "Event not found");
                return ResponseEntity.status(404).body(jsonResponse);
            }
            if (role.equals("admin") && currEvent.getOrganizer().getUserId() == Integer.parseInt(id)) {
                jsonResponse.put("message", "You are not allowed to register for this event since you are the organizer for this event");
                return ResponseEntity.status(403).body(jsonResponse);
            }
            boolean isRegistered = eventdao.isRegisteredForEvent(new User(Integer.parseInt(id)), new Event(eventId), userdao);
            if (isRegistered) {
                jsonResponse.put("message", "You are not allowed to register for this event since you have already registered");
                return ResponseEntity.status(403).body(jsonResponse);
            }
            eventdao.registerForEvent(new User(Integer.parseInt(id)), new Event(eventId), userdao);
            jsonResponse.put("message", "Successfully added event");
            return ResponseEntity.status(200).body(jsonResponse);
        }
        catch(HibernateException e) {
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
        catch(Exception e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
    }

    @DeleteMapping("/{eventId}/unregister")
    public ResponseEntity<Map<String, Object>> unregisterFromEvent(Eventdao eventdao, Userdao userdao, @PathVariable String eventId, @RequestAttribute String id) {
        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            User user = userdao.getUserByUserId(Integer.parseInt(id));
            Event event = eventdao.getOneEvent(Integer.parseInt(eventId));
            boolean success = eventdao.deleteRegistration(user, event);
            if (!success) {
                jsonResponse.put("message", "Something went wrong");
                return ResponseEntity.status(500).body(jsonResponse);
            }
            jsonResponse.put("event", event);
            return ResponseEntity.status(200).body(jsonResponse);
        }
        catch (Exception e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
    }
}
