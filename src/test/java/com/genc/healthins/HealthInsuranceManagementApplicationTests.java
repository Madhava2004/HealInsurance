package com.genc.healthins;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.genc.healthins.controller.AuthController;
import com.genc.healthins.controller.ClaimController;
import com.genc.healthins.model.User;
import com.genc.healthins.service.ClaimService;
import com.genc.healthins.service.UserService;
import com.genc.healthins.repository.UserPolicyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class HealthInsuranceManagementApplicationTests {

    private MockMvc mockMvcAuth;
    private MockMvc mockMvcClaim;

    @Mock
    private UserService userService;

    @Mock
    private ClaimService claimService;

    @Mock
    private UserPolicyRepository userPolicyRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private ClaimController claimController;

    private User mockUser;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // Manually build MockMvc for each controller to bypass AutoConfiguration issues
        mockMvcAuth = MockMvcBuilders.standaloneSetup(authController).build();
        mockMvcClaim = MockMvcBuilders.standaloneSetup(claimController).build();

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("test@example.com");
        mockUser.setRole("USER");
        mockUser.setPassword("hashedPassword");

        session = new MockHttpSession();
        session.setAttribute("loggedInUser", mockUser);
    }

    @Test
    @DisplayName("TC-01: Login Failure with Invalid Email")
    void testLoginInvalidUser() throws Exception {
        when(userService.findByEmail("wrong@test.com"))
        	.thenReturn(Optional.empty());

        mockMvcAuth.perform(post("/login")
                .param("email", "wrong@test.com")
                .param("password", "any"))
                .andExpect(flash().attribute("error", "Invalid credentials"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("TC-02: Claim Validation - Negative Amount")
    void testSubmitClaimNegativeAmount() throws Exception {
        mockMvcClaim.perform(post("/user/claims/submit")
                .session(session)
                .param("policyId", "1")
                .param("amount", "-100.00")
                .param("description", "Negative test")
                .param("incidentDate", LocalDate.now().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Error: Claim amount cannot be negative."));
    }

    @Test
    @DisplayName("TC-03: Claim Validation - Future Date")
    void testSubmitClaimFutureDate() throws Exception {
        String futureDate = LocalDate.now().plusDays(5).toString();

        mockMvcClaim.perform(post("/user/claims/submit")
                .session(session)
                .param("policyId", "1")
                .param("amount", "500.00")
                .param("description", "Future test")
                .param("incidentDate", futureDate))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Error: Incident date cannot be in the future."));
    }

    @Test
    @DisplayName("TC-04: Admin Login Redirection Logic")
    void testAdminRedirect() throws Exception {
        User admin = new User();
        admin.setRole("ADMIN");
        admin.setPassword("hashed");

        when(userService.findByEmail("admin@hims.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        mockMvcAuth.perform(post("/login")
                .param("email", "admin@hims.com")
                .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }
}