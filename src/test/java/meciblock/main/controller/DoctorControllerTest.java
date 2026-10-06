package meciblock.main.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DoctorController.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
void dashboardTest() throws Exception {

    long start = System.nanoTime();

    mockMvc.perform(get("/doctor/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("doctor/dashboard"));

    long end = System.nanoTime();

    long responseTime = TimeUnit.NANOSECONDS.toMillis(end - start);

    System.out.println("Dashboard Response Time: " + responseTime + " ms");
}

    @Test
    void patientsTest() throws Exception {

        long start = System.nanoTime();

        mockMvc.perform(get("/doctor/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/patients"));

        long end = System.nanoTime();

        long responseTime = TimeUnit.NANOSECONDS.toMillis(end - start);

        System.out.println("Patients Response Time: " + responseTime + " ms");

    }

    @Test
    void createPrescriptionTest() throws Exception {

        long start = System.nanoTime();

        mockMvc.perform(get("/doctor/create-prescription"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/create-prescription"));

        long end = System.nanoTime();

        long responseTime = TimeUnit.NANOSECONDS.toMillis(end - start);

        System.out.println("Create Prescription Response Time: " + responseTime + " ms");

    }

    @Test
    void prescriptionsTest() throws Exception {

        long start = System.nanoTime();

        mockMvc.perform(get("/doctor/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/prescriptions"));

        long end = System.nanoTime();

        long responseTime = TimeUnit.NANOSECONDS.toMillis(end - start);

        System.out.println("Prescriptions Response Time: " + responseTime + " ms");

    }
}