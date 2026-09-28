package com.eventease;

import com.eventease.dto.EventCreateRequest;
import com.eventease.dto.OrganizerCreateRequest;
import com.eventease.dto.RegistrationRequest;
import com.eventease.dto.StudentCreateRequest;
import com.eventease.entity.Event;
import com.eventease.entity.Organizer;
import com.eventease.entity.Registration;
import com.eventease.entity.RegistrationStatus;
import com.eventease.entity.Student;
import com.eventease.repository.EventRepository;
import com.eventease.repository.OrganizerRepository;
import com.eventease.repository.RegistrationRepository;
import com.eventease.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class EventRegistrationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    private Organizer testOrganizer;
    private Student testStudent1;
    private Student testStudent2;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        studentRepository.deleteAll();
        organizerRepository.deleteAll();

        testOrganizer = organizerRepository.save(new Organizer("Computer Science Dept", "cs@college.edu", "Engineering"));
        testStudent1 = studentRepository.save(new Student("Alice Johnson", "alice@college.edu", "CS001"));
        testStudent2 = studentRepository.save(new Student("Bob Smith", "bob@college.edu", "CS002"));
    }

    // ==========================================
    // NORMAL CASES
    // ==========================================

    @Test
    @DisplayName("Normal: Successfully create an event")
    void testCreateEventSuccess() throws Exception {
        EventCreateRequest request = new EventCreateRequest(
                "Hackathon 2026",
                LocalDate.now().plusDays(10),
                "Main Auditorium",
                50,
                testOrganizer.getId()
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title", is("Hackathon 2026")))
                .andExpect(jsonPath("$.venue", is("Main Auditorium")))
                .andExpect(jsonPath("$.maxSeats", is(50)))
                .andExpect(jsonPath("$.availableSeats", is(50)))
                .andExpect(jsonPath("$.activeRegistrationsCount", is(0)))
                .andExpect(jsonPath("$.organizerId", is(testOrganizer.getId().intValue())));
    }

    @Test
    @DisplayName("Normal: Successfully register a student")
    void testRegisterStudentSuccess() throws Exception {
        Event event = eventRepository.save(new Event("AI Workshop", LocalDate.now().plusDays(5), "Lab 1", 20, testOrganizer));
        RegistrationRequest request = new RegistrationRequest(testStudent1.getId(), event.getId());

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.studentId", is(testStudent1.getId().intValue())))
                .andExpect(jsonPath("$.studentName", is("Alice Johnson")))
                .andExpect(jsonPath("$.eventId", is(event.getId().intValue())))
                .andExpect(jsonPath("$.eventTitle", is("AI Workshop")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Normal: Successfully view registered participants")
    void testViewRegisteredParticipants() throws Exception {
        Event event = eventRepository.save(new Event("Coding Contest", LocalDate.now().plusDays(7), "Hall B", 10, testOrganizer));
        registrationRepository.save(new Registration(testStudent1, event));
        registrationRepository.save(new Registration(testStudent2, event));

        mockMvc.perform(get("/api/events/" + event.getId() + "/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Alice Johnson")))
                .andExpect(jsonPath("$[1].name", is("Bob Smith")));
    }

    @Test
    @DisplayName("Normal: Successfully cancel a registration")
    void testCancelRegistrationSuccess() throws Exception {
        Event event = eventRepository.save(new Event("Robotics Seminar", LocalDate.now().plusDays(15), "Room 101", 10, testOrganizer));
        Registration reg = registrationRepository.save(new Registration(testStudent1, event));

        mockMvc.perform(put("/api/registrations/" + reg.getId() + "/cancel?studentId=" + testStudent1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reg.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.cancellationDate").exists());
    }

    @Test
    @DisplayName("Normal: Successfully register another student after cancellation (frees a seat)")
    void testRegisterAnotherStudentAfterCancellation() throws Exception {
        // Event with capacity = 1
        Event event = eventRepository.save(new Event("Single Seat Masterclass", LocalDate.now().plusDays(10), "Studio 1", 1, testOrganizer));
        Registration reg1 = registrationRepository.save(new Registration(testStudent1, event));

        // Attempting to register student2 before cancellation should fail because it's full
        RegistrationRequest regRequest2 = new RegistrationRequest(testStudent2.getId(), event.getId());
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Event 'Single Seat Masterclass' is full. Maximum capacity of 1 seats has been reached.")));

        // Student 1 cancels registration
        mockMvc.perform(delete("/api/registrations/" + reg1.getId() + "?studentId=" + testStudent1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Now student 2 registers successfully into the freed seat!
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId", is(testStudent2.getId().intValue())))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    // ==========================================
    // EDGE / ERROR CASES
    // ==========================================

    @Test
    @DisplayName("Edge: Register when event is already full")
    void testRegisterWhenEventIsAlreadyFull() throws Exception {
        Event event = eventRepository.save(new Event("Micro Summit", LocalDate.now().plusDays(5), "Room 3", 1, testOrganizer));
        registrationRepository.save(new Registration(testStudent1, event));

        RegistrationRequest regRequest2 = new RegistrationRequest(testStudent2.getId(), event.getId());
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Event Full")))
                .andExpect(jsonPath("$.message", is("Event 'Micro Summit' is full. Maximum capacity of 1 seats has been reached.")));
    }

    @Test
    @DisplayName("Edge: Register the same student twice (duplicate registration)")
    void testRegisterSameStudentTwice() throws Exception {
        Event event = eventRepository.save(new Event("Web Dev Workshop", LocalDate.now().plusDays(8), "Lab 2", 10, testOrganizer));
        registrationRepository.save(new Registration(testStudent1, event));

        RegistrationRequest duplicateRequest = new RegistrationRequest(testStudent1.getId(), event.getId());
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    @DisplayName("Edge: Register with invalid input (null fields)")
    void testRegisterWithInvalidInput() throws Exception {
        RegistrationRequest invalidRequest = new RegistrationRequest(null, null);

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.validationErrors.studentId").exists())
                .andExpect(jsonPath("$.validationErrors.eventId").exists());
    }

    @Test
    @DisplayName("Edge: Register for a non-existing event")
    void testRegisterForNonExistingEvent() throws Exception {
        RegistrationRequest request = new RegistrationRequest(testStudent1.getId(), 99999L);

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Event not found with ID: 99999")));
    }

    @Test
    @DisplayName("Edge: Register a non-existing student")
    void testRegisterNonExistingStudent() throws Exception {
        Event event = eventRepository.save(new Event("Cloud Computing", LocalDate.now().plusDays(12), "Lab 3", 10, testOrganizer));
        RegistrationRequest request = new RegistrationRequest(99999L, event.getId());

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Student not found with ID: 99999")));
    }

    @Test
    @DisplayName("Edge: Cancel a non-existing registration")
    void testCancelNonExistingRegistration() throws Exception {
        mockMvc.perform(put("/api/registrations/88888/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Registration not found with ID: 88888")));
    }

    @Test
    @DisplayName("Edge: Cancel after or on the event date (rejected)")
    void testCancelAfterEventDate() throws Exception {
        // Event is today or in the past
        Event pastEvent = eventRepository.save(new Event("Past Symposium", LocalDate.now().minusDays(1), "Auditorium", 20, testOrganizer));
        Registration pastReg = registrationRepository.save(new Registration(testStudent1, pastEvent));

        mockMvc.perform(put("/api/registrations/" + pastReg.getId() + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Cancellation Not Allowed")));

        // Event is today (rule: cannot cancel on or after event date)
        Event todayEvent = eventRepository.save(new Event("Today Seminar", LocalDate.now(), "Auditorium", 20, testOrganizer));
        Registration todayReg = registrationRepository.save(new Registration(testStudent1, todayEvent));

        mockMvc.perform(put("/api/registrations/" + todayReg.getId() + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Cancellation Not Allowed")));
    }

    @Test
    @DisplayName("Edge: Student can cancel only their own registration")
    void testCancelByUnauthorizedStudent() throws Exception {
        Event event = eventRepository.save(new Event("Cybersecurity Conference", LocalDate.now().plusDays(20), "Main Hall", 30, testOrganizer));
        Registration reg = registrationRepository.save(new Registration(testStudent1, event));

        // Student 2 tries to cancel Student 1's registration
        mockMvc.perform(put("/api/registrations/" + reg.getId() + "/cancel?studentId=" + testStudent2.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid Operation")));
    }

    @Test
    @DisplayName("Edge: Event creation validation (empty title, negative seats, past date, missing organizer)")
    void testCreateEventValidationErrors() throws Exception {
        EventCreateRequest request = new EventCreateRequest(
                "", // empty title
                null, // null date
                "", // empty venue
                -5, // non-positive max seats
                null // null organizer
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.venue").exists())
                .andExpect(jsonPath("$.validationErrors.eventDate").exists())
                .andExpect(jsonPath("$.validationErrors.maxSeats").exists())
                .andExpect(jsonPath("$.validationErrors.organizerId").exists());
    }
}
