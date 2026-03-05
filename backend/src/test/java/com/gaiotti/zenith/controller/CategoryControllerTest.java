package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.config.SecurityConfig;
import com.gaiotti.zenith.config.AllowedOriginsProvider;
import com.gaiotti.zenith.dto.request.CreateCategoryRequest;
import com.gaiotti.zenith.dto.response.CategoryResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.security.UserDetailsServiceImpl;
import com.gaiotti.zenith.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthUtils authUtils;

    @MockBean
    private AllowedOriginsProvider allowedOriginsProvider;

    @MockBean
    private CategoryService categoryService;

    @Test
    void listCategories_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ledgers/1/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void listCategories_AuthenticatedMember_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Food")
                .color("#FF5733")
                .createdAt(LocalDateTime.now())
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(categoryService.listCategories(eq(1L), any(User.class))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/ledgers/1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"));
    }

    @Test
    @WithMockUser
    void listCategories_NonMember_Returns403() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(categoryService.listCategories(eq(1L), any(User.class)))
                .thenThrow(new com.gaiotti.zenith.exception.AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/v1/ledgers/1/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void createCategory_ValidRequest_Returns201() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Food")
                .color("#FF5733")
                .createdAt(LocalDateTime.now())
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(categoryService.createCategory(eq(1L), any(CreateCategoryRequest.class), any(User.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/ledgers/1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Food",
                                    "color": "#FF5733"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    @WithMockUser
    void createCategory_InvalidColor_Returns400() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/ledgers/1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Food",
                                    "color": "invalid"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deleteCategory_AuthenticatedMember_Returns204() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);

        mockMvc.perform(delete("/api/v1/ledgers/1/categories/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteCategory_NonMember_Returns403() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        doThrow(new com.gaiotti.zenith.exception.AccessDeniedException("Access denied"))
                .when(categoryService).deleteCategory(eq(1L), eq(1L), any(User.class));

        mockMvc.perform(delete("/api/v1/ledgers/1/categories/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
