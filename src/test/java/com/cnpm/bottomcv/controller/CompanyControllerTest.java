package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.CompanyRequest;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    private CompanyResponse companyResponse;
    private CompanyRequest companyRequest;

    @BeforeEach
    void setUp() {
        companyResponse = new CompanyResponse();
        companyResponse.setId(1L);
        companyResponse.setName("Test Company");
        companyResponse.setAddress("123 Test St");

        companyRequest = new CompanyRequest();
        companyRequest.setName("Test Company");
        companyRequest.setAddress("123 Test St");
    }

    @Test
    void testCreateCompany() throws Exception {
        when(companyService.createCompany(any(CompanyRequest.class))).thenReturn(companyResponse);

        mockMvc.perform(post("/api/companies/back/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(companyResponse)));
    }

    @Test
    void testUpdateCompany() throws Exception {
        when(companyService.updateCompany(anyLong(), any(CompanyRequest.class))).thenReturn(companyResponse);

        mockMvc.perform(put("/api/companies/back/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(companyResponse)));
    }

    @Test
    void testDeleteCompany() throws Exception {
        doNothing().when(companyService).deleteCompany(anyLong());

        mockMvc.perform(delete("/api/companies/back/companies/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetCompanyByIdForBack() throws Exception {
        when(companyService.getCompanyById(anyLong())).thenReturn(companyResponse);

        mockMvc.perform(get("/api/companies/back/companies/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(companyResponse)));
    }

    @Test
    void testGetAllCompaniesForBack() throws Exception {
        List<CompanyResponse> companyResponses = new ArrayList<>();
        companyResponses.add(companyResponse);
        ListResponse<CompanyResponse> listResponse = new ListResponse<>();
        listResponse.setContent(companyResponses);
        listResponse.setTotalElements(1L);

        when(companyService.getAllCompanies(anyInt(), anyInt(), anyString(), anyString())).thenReturn(listResponse);

        mockMvc.perform(get("/api/companies/back/companies"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(listResponse)));
    }

    @Test
    void testGetCompanyByIdForFront() throws Exception {
        when(companyService.getCompanyById(anyLong())).thenReturn(companyResponse);

        mockMvc.perform(get("/api/companies/front/companies/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(companyResponse)));
    }

    @Test
    void testGetAllCompaniesForFront() throws Exception {
        List<CompanyResponse> companyResponses = new ArrayList<>();
        companyResponses.add(companyResponse);
        ListResponse<CompanyResponse> listResponse = new ListResponse<>();
        listResponse.setContent(companyResponses);
        listResponse.setTotalElements(1L);

        when(companyService.getAllCompanies(anyInt(), anyInt(), anyString(), anyString())).thenReturn(listResponse);

        mockMvc.perform(get("/api/companies/front/companies"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(listResponse)));
    }
}