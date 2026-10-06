package meciblock.main.controller;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
@WebMvcTest(AdminController.class)
public class AdminControllerTest {
    @Autowired 
    private MockMvc mockMvc;
    @Test
    void testGetAudit() throws Exception {
        long start = System.nanoTime();
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit"));
        long end = System.nanoTime();
        long responseTime = TimeUnit.NANOSECONDS.toMillis(end - start);
        System.out.println("Audit Response Time: " + responseTime + " ms");
        assertTrue(responseTime < 10000);
        assertAll(
                () -> assertTrue(responseTime < 10000, "Response time exceeded 10 seconds"),
                () -> assertTrue(responseTime >= 0, "Response time is negative")
        );
    }

    @Test
    void testGetBlockChain() throws Exception {
        long start = System.nanoTime();
        mockMvc.perform(get("/admin/blockchain"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/blockchain"));
        long end = System.nanoTime();
        long responseTime = TimeUnit.NANOSECONDS.toMillis(end - start);
        System.out.println("BlockChain Response Time: " + responseTime + " ms");
    }



    @Test
    void testGetMethodName() throws Exception {
        long start = System.nanoTime();
        mockMvc.perform(get("/admin/method-name"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/method-name"));
        long end = System.nanoTime();
        long responseTime = TimeUnit.NANOSECONDS.toMillis(end - start);
        System.out.println("MethodName Response Time: " + responseTime + " ms");    
    }

    @Test
    void testGetUsers() throws Exception {
        long start = System.nanoTime();
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
        long end = System.nanoTime();
        long responseTime = TimeUnit.NANOSECONDS.toMillis(end - start);
        System.out.println("Users Response Time: " + responseTime + " ms");
    }
}
