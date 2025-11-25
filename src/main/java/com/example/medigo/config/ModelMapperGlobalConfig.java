package com.example.medigo.config;

import com.example.medigo.domain.Medico;
import com.example.medigo.domain.Paciente;
import com.example.medigo.dto.response.MedicoResponseDto;
import com.example.medigo.dto.response.PacienteResponseDto;
import com.example.medigo.dto.response.MedicoSearchResponseDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperGlobalConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STANDARD)
                .setSkipNullEnabled(true);
        
        // Explicit mapping for Medico to MedicoSearchResponseDto
        mapper.createTypeMap(Medico.class, MedicoSearchResponseDto.class)
                .addMapping(Medico::getNumeroColegiado, MedicoSearchResponseDto::setNumeroColegiado)
                .addMapping(Medico::getBio, MedicoSearchResponseDto::setBio)
                .addMapping(Medico::getEstadoCuenta, MedicoSearchResponseDto::setEstadoCuenta);
        
        // Explicit mapping for Medico to MedicoResponseDto
        mapper.createTypeMap(Medico.class, MedicoResponseDto.class)
                .addMapping(Medico::getNumeroColegiado, MedicoResponseDto::setNumeroColegiado)
                .addMapping(Medico::getBio, MedicoResponseDto::setBio)
                .addMapping(Medico::getEstadoCuenta, MedicoResponseDto::setEstadoCuenta);
        
        // Explicit mapping for Paciente to PacienteResponseDto
        mapper.createTypeMap(Paciente.class, PacienteResponseDto.class)
                .addMapping(Paciente::getEstadoCuenta, PacienteResponseDto::setEstadoCuenta);
        
        return mapper;
    }
}